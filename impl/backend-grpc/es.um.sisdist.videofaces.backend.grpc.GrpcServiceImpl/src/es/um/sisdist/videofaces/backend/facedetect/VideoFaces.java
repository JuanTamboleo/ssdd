package es.um.sisdist.videofaces.backend.facedetect;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.VideoPositionListener;
import org.openimaj.video.xuggle.XuggleVideo;

import com.fasterxml.jackson.databind.cfg.ContextAttributes.Impl;

import es.um.sisdist.videofaces.backend.grpc.VideoAvailability;
import es.um.sisdist.videofaces.backend.impl.AppLogicImpl;
import io.grpc.stub.StreamObserver;

/**
 * OpenIMAJ Hello world!
 *
 */
public class VideoFaces implements Runnable {
	private InputStream inputStream;
	private StreamObserver<VideoAvailability> responseObserver;
	private AppLogicImpl impl = AppLogicImpl.getInstance();
	private String videoID;

	public VideoFaces(InputStream inputStream, StreamObserver<VideoAvailability> responseObserver, String videoID) {
		this.inputStream = inputStream;
		this.responseObserver = responseObserver;
		this.videoID = videoID;
		System.out.println("En grpc la id es ---->" + videoID);
	}

	@Override
	public void run() {
		// VideoCapture vc = new VideoCapture( 320, 240 );
		// VideoDisplay<MBFImage> video = VideoDisplay.createVideoDisplay( vc );

		String path = "videos/a.mp4";
		File file = new File(path);
		try {
			Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		Video<MBFImage> video = new XuggleVideo(path);
		VideoDisplay<MBFImage> vd = VideoDisplay.createOffscreenVideoDisplay(video);
//		try {
//			System.out.println(inputStream.readAllBytes().length);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		// El Thread de procesamiento de vídeo se termina al terminar el vídeo.
		vd.setEndAction(EndAction.CLOSE_AT_END);

		vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
			// Número de imagen
			int imgn = 0;

			@Override
			public void beforeUpdate(MBFImage frame) {
				FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
				List<DetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(frame));

				for (DetectedFace face : faces) {
					frame.drawShape(face.getBounds(), RGBColour.RED);
					try {
						// También permite enviar la imagen a un OutputStream
						ImageUtilities.write(frame.extractROI(face.getBounds()),
								new File(String.format("photos/img%05d.jpg", imgn++)));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("!");
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}
		});

		vd.addVideoPositionListener(new VideoPositionListener() {
			@Override
			public void videoAtStart(VideoDisplay<? extends Image<?, ?>> vd) {
			}

			@Override
			public void videoAtEnd(VideoDisplay<? extends Image<?, ?>> vd) {
				System.out.println("End of video");
				file.deleteOnExit();
//				file.delete();
				impl.addAllPhotos(videoID);
				responseObserver.onNext(VideoAvailability.newBuilder().setAvailable(true).build());
				responseObserver.onCompleted();
			}
		});

		System.out.println("Fin.");

	}
}
