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
			ByteString completo = ByteString.EMPTY;

			@Override
			public void onNext(VideoAndChunkData value) {
				if (value.hasVideoid()) {
					mensaje = value.getVideoid();
				} else {
					completo = completo.concat(value.getData());
				}
			}

			@Override
			public void onError(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void onCompleted() {
				new Thread(new VideoFaces(completo.newInput(), responseObserver, mensaje)).start();
			}

		};
	}

}