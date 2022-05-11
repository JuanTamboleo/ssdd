package es.um.sisdist.videofaces.backend.grpc;

import com.google.protobuf.Empty;

import es.um.sisdist.videofaces.backend.grpc.GrpcServiceGrpc.GrpcServiceImplBase;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcServiceServer {
	private int port = 50051;
	private Server server;

	private void start() throws Exception {
		server = ServerBuilder.forPort(port).addService(new IsAvailable()).build().start();
		System.out.println("Servidor escuchando en " + port);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				GrpcServiceServer.this.stop();
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static void main(String[] args) throws Exception {
		final GrpcServiceServer server = new GrpcServiceServer();
		server.start();
		server.blockUntilShutdown();
	}

	private class IsAvailable extends GrpcServiceImplBase {

		@Override
		public void isVideoReady(VideoSpec request, StreamObserver<VideoAvailability> responseObserver) {
			System.out.println("Petición de comprobar vídeo" + request.getId());
			VideoAvailability reply = VideoAvailability.newBuilder().setAvailable(true).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

		@Override
		public StreamObserver<VideoAndChunkData> processVideo(StreamObserver<Empty> responseObserver) {
			return new StreamObserver<VideoAndChunkData>() {
				String mensaje = "";

				@Override
				public void onNext(VideoAndChunkData value) {
					if (value.hasVideoid()) {
						mensaje += value.getVideoid();
					}
					System.out.println("Mensaje por partes" + mensaje);
				}

				@Override
				public void onError(Throwable t) {
					System.out.println("Error");
				}

				@Override
				public void onCompleted() {
					System.out.println("Mensaje completo: " + mensaje);
					responseObserver.onNext(null);
					responseObserver.onCompleted();
				}

			};
		}

	}

}
