/**
 *
 */
package es.um.sisdist.videofaces.backend.Service.impl;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.Empty;

import es.um.sisdist.videofaces.backend.dao.DAOFactoryImpl;
import es.um.sisdist.videofaces.backend.dao.IDAOFactory;
import es.um.sisdist.videofaces.backend.dao.models.User;
import es.um.sisdist.videofaces.backend.dao.user.IUserDAO;
import es.um.sisdist.videofaces.backend.grpc.*;
import es.um.sisdist.videofaces.models.UserDTO;
import es.um.sisdist.videofaces.models.UserDTOUtils;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl {
	IDAOFactory daoFactory;
	IUserDAO dao;

	private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

	private final ManagedChannel channel;
	private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
	private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

	static AppLogicImpl instance = new AppLogicImpl();

	private AppLogicImpl() {
		daoFactory = new DAOFactoryImpl();
		dao = daoFactory.createSQLUserDAO();

		Optional<String> grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
		Optional<String> grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

		channel = ManagedChannelBuilder
				.forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
				// Channels are secure by default (via SSL/TLS). For the example we disable TLS
				// to avoid
				// needing certificates.
				.usePlaintext().build();
		blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
		asyncStub = GrpcServiceGrpc.newStub(channel);
	}

	public static AppLogicImpl getInstance() {
		return instance;
	}

	public Optional<User> getUserByEmail(String userId) {
		Optional<User> u = dao.getUserByEmail(userId);
		return u;
	}

	public Optional<User> getUserById(String userId) {
		return dao.getUserById(userId);
	}

	public boolean isVideoReady(String videoId) {
		// Test de grpc, puede hacerse con la BD
		VideoAvailability available = blockingStub.isVideoReady(VideoSpec.newBuilder().setId(videoId).build());
		return available.getAvailable();
	}
	
	public void variosIDs() throws InterruptedException {
		final CountDownLatch finishLatch = new CountDownLatch(1);

		StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {

			@Override
			public void onNext(Empty value) {
				System.out.println("Cliente onNext");

			}

			@Override
			public void onError(Throwable t) {
				System.out.println("Cliente error");
				finishLatch.countDown();

			}

			@Override
			public void onCompleted() {
				System.out.println("Cliente acaba");
				finishLatch.countDown();
			}

		};
		StreamObserver<VideoAndChunkData> requestObserver = asyncStub.processVideo(responseObserver);

		try {
			for (int i = 0; i < 10; i++) {
				VideoAndChunkData strings = VideoAndChunkData.newBuilder().setVideoid("DesdeElCliente" + i).build();
				requestObserver.onNext(strings);
				Thread.sleep(500);
				if (finishLatch.getCount() == 0) {
					return;
				}
			}
		} catch (RuntimeException e) {
			requestObserver.onError(e);
			throw e;
		}
		requestObserver.onCompleted();
		finishLatch.await(1, TimeUnit.MINUTES);
	}

	// El frontend, a través del formulario de login,
	// envía el usuario y pass, que se convierte a un DTO. De ahí
	// obtenemos la consulta a la base de datos, que nos retornará,
	// si procede,
	public Optional<User> checkLogin(String email, String pass) {
		Optional<User> u = dao.getUserByEmail(email);
		
		if (u.isPresent()) {
			String hashed_pass = User.md5pass(pass);
			if (0 == hashed_pass.compareTo(u.get().getPassword_hash())) {
				return u;
			}
		}

		return Optional.empty();
	}

	// Registro de usuario
	public Optional<User> register(String email, String username, String pass) {
		Optional<User> u = dao.addUser(email, username, pass);
		return u;
	}
	
	public void deleteUsers() {
		dao.deleteUsers();
	}
	
	public void printUsers() {
		dao.printUsers();
	}
}
