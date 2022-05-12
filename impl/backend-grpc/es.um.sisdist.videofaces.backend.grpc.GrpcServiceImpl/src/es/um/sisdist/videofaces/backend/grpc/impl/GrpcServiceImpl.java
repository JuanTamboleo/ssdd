package es.um.sisdist.videofaces.backend.grpc.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;

import es.um.sisdist.videofaces.backend.facedetect.VideoFaces;
import es.um.sisdist.videofaces.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.videofaces.backend.grpc.VideoAndChunkData;
import es.um.sisdist.videofaces.backend.grpc.VideoAvailability;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase {
	private Logger logger;

	public GrpcServiceImpl(Logger logger) {
		super();
		this.logger = logger;
	}

	@Override
	public StreamObserver<VideoAndChunkData> processVideo(StreamObserver<VideoAvailability> responseObserver) {
		return new StreamObserver<VideoAndChunkData>() {
			String mensaje = "";
//			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ByteString completo = ByteString.EMPTY;

			@Override
			public void onNext(VideoAndChunkData value) {
				if (value.hasVideoid()) {
					mensaje += value.getVideoid();
					System.out.println("Id del vídeo " + mensaje);
				} else {
					completo = completo.concat(value.getData());
//					try {

//						outputStream.write(value.getData().toByteArray());
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
					System.out.println("Tamaño del trozo: " + value.getData().size());
				}
			}

			@Override
			public void onError(Throwable t) {
				System.out.println("Error");
			}

			@Override
			public void onCompleted() {
				System.out.println("Mensaje completo: " + mensaje);
				new Thread(new VideoFaces(completo.newInput())).start();
//				new Thread(new VideoFaces(new ByteArrayInputStream(outputStream.toByteArray()))).start();
				responseObserver.onNext(VideoAvailability.newBuilder().setAvailable(true).build());
				responseObserver.onCompleted();
			}

		};
	}

//	@Override
//	public void isVideoReady(VideoSpec request, StreamObserver<VideoAvailability> responseObserver) {
//		System.out.println("PeticiÃ³n de comprobar vÃ­deo" + request.getId());
//		VideoAvailability reply = VideoAvailability.newBuilder().setAvailable(true).build();
//		responseObserver.onNext(reply);
//		responseObserver.onCompleted();
//	}
}