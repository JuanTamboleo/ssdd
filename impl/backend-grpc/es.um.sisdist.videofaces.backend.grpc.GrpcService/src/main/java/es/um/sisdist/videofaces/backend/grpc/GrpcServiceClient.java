package es.um.sisdist.videofaces.backend.grpc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

/**
 * A simple client that requests a greeting from the {@link CollageServer}.
 */
public class GrpcServiceClient {

	private final ManagedChannel channel;
	private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
	private final GrpcServiceGrpc.GrpcServiceStub asynStub;

	public GrpcServiceClient(String host, int port) {
		channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
		blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
		asynStub = GrpcServiceGrpc.newStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public void hola(String string) {
		System.out.println("Mando: " + string);
		VideoSpec request = VideoSpec.newBuilder().setId("EL 44").build();
		VideoAvailability response;
		try {
			response = blockingStub.isVideoReady(request);
		} catch (StatusRuntimeException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Respuesta: " + response.getAvailable());
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
		StreamObserver<VideoAndChunkData> requestObserver = asynStub.processVideo(responseObserver);

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

	public static void main(String[] args) throws InterruptedException {
		GrpcServiceClient client = new GrpcServiceClient("localhost", 50051);
		try {
			String mando = "pruebaMandar";
//			client.hola(mando);
			client.variosIDs();
		} finally {
			client.shutdown();
		}
	}

}
