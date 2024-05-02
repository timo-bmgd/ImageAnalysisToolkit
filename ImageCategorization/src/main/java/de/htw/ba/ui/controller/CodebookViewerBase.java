package de.htw.ba.ui.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.controlsfx.control.GridView;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.cell.ImageGridCell;

import de.htw.ba.model.ImageData;
import de.htw.ba.model.PixelCodebook;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * Codebook viewer. Lists all image files in a directory.
 * Provides an image and image patch viewer. Furthermore is visualizes the codebook.
 * 
 * The controller handles all events of the CodebookViewer.fxml view.
 * 
 * @author Nico Hezel
 */
public abstract class CodebookViewerBase {
	
	/**
	 * Map of all images, maps from image filename to the corresponding ImageData
	 */
	private Map<String, ImageData> imageDataList = new HashMap<>();

	@FXML
	protected ListView<String> imageFileList;

	@FXML
	protected ImageView imagePane;
	
	@FXML
	protected ImageView gridImagePane;
	
	@FXML
	protected TabPane codebookTabPane;
	
	
	
	@FXML
	protected ImageView codeImagePane;
	
	@FXML
	protected ImageView recodedImagePane;
	
	@FXML
	protected Label codebookLabel;	
	
	@FXML
	protected Label patchSizeLabel;
	
	@FXML
	protected Slider patchSizeSlider;
	
	@FXML
	protected Label codebookSizeLabel;
	
	@FXML
	protected Slider codebookSizeSlider;
	
	
	
	
	@FXML
	protected ImageView globalCodeImagePane;
	
	@FXML
	protected ImageView globalRecodedImagePane;
	
	@FXML
	protected Label globalCodebookLabel;	
	
	@FXML
	protected Label globalPatchSizeLabel;
	
	@FXML
	protected Slider globalPatchSizeSlider;
	
	@FXML
	protected Label globalCodebookSizeLabel;
	
	@FXML
	protected Slider globalCodebookSizeSlider;
	
	@FXML
	protected Button computeHistogramBtn;
	
	@FXML
	protected Label histogramInfoLabel;
	

	
	@FXML
	protected ToggleSwitch histogramToggle;
	
	@FXML
	protected StackPane rankListPane;	
	protected ObservableList<Image> rankList = FXCollections.observableArrayList();
	
	
	protected PixelCodebook globalCodebook;
	protected Thread codebookThread;
	protected Timer codebookBuildTimer;
	
	protected ImageData query;
	
	/**
	 * Gets called once at program start
	 * 
	 * @throws URISyntaxException 
	 */
	@FXML
	public void initialize() throws URISyntaxException {
		registerEventHandler();

		// setup an image grid
		EventHandler<MouseEvent> clickEventHandler = (click) -> {
			
			// If a double click is registered a search will be triggered.
			if (click.getClickCount() == 2) {
				final ImageGridCell cell = (ImageGridCell) click.getSource();
				final Image image = cell.getItem();

				// select the filename of the clicked image
				final Optional<ImageData> imageData = imageDataList.values().stream().filter(data -> data.getImage() == image).findAny();
				if(imageData.isPresent()) {
					String filename = imageData.get().getName();
					imageFileList.getSelectionModel().select(filename);
					imageFileList.scrollTo(filename);
				}
			}
		};
		
		GridView<Image> rankGrid = new GridView<>(rankList);
		rankGrid.setCellFactory(gridView -> {
			ImageGridCell cell = new ImageGridCell();
			cell.setOnMouseClicked(clickEventHandler);
			return cell;
		});
		rankListPane.getChildren().add(rankGrid);
		
		// load the images from the dataset directory
		try (DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get("dataset"), "*.{jpg,jpeg,png}")) {
			for (Path imageFile : files) {
				final String filename = imageFile.getFileName().toString();
				final Image image = new Image(imageFile.toUri().toString());
				imageDataList.put(filename, new ImageData(filename, imageFile, image));
				imageFileList.getItems().add(filename);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	/**
	 * Register all event listeners
	 */
	private void registerEventHandler() {

		// call onComputeHistogramClick if the computeHistogramBtn gets pressed
		computeHistogramBtn.setOnAction((event) -> buildCodebookAndComputeHistograms());

		// call onImageFileListChange if a item in the imageFileList view gets selected
		imageFileList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			updateImageVisualization();
		});
	
		// register clicks events
		imageFileList.setOnMouseClicked((click) -> {
			
			// If a double click is registered a search will be triggered.
			if (click.getClickCount() == 2) {
				
				// get the query
				final String filename = imageFileList.getSelectionModel().getSelectedItem();
				query = imageDataList.get(filename);
				rankByQuery(query);
			}
		});	
		
		// trigger re-ranking and show either the images or the histogram
		histogramToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
			rankByQuery(query);
		});
		
		// tab changes
		codebookTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			updateImageVisualization();
		});
		
		// update labels when changing the sliders
		Function<Slider, IntegerBinding> sliderBinder = (Slider slider) -> {
			return new IntegerBinding() {
			     {
			         super.bind(slider.valueProperty());
			     }

			     @Override
			     protected int computeValue() {
			         return 2 << slider.valueProperty().getValue().intValue();
			     }

			     @Override
			     public void dispose() {
			         super.unbind(slider.valueProperty());
			     }
			};
		};
		patchSizeLabel.textProperty().bind(Bindings.format("Patch Size = %d", sliderBinder.apply(patchSizeSlider)));
		globalPatchSizeLabel.textProperty().bind(Bindings.format("Patch Size = %d", sliderBinder.apply(globalPatchSizeSlider)));
		codebookSizeLabel.textProperty().bind(Bindings.format("Codebook Size = %.0f", codebookSizeSlider.valueProperty()));
		globalCodebookSizeLabel.textProperty().bind(Bindings.format("Codebook Size = %.0f", globalCodebookSizeSlider.valueProperty()));
		
		patchSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			updateImageVisualization();
		});
		
		globalPatchSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			updateImageVisualization();
		});
		
		codebookSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			updateImageVisualization();
		});
	}
	
	/**
	 * Trigger a retrieval process with this query.
	 * 
	 * @param query
	 */
	protected void rankByQuery(ImageData query) {
		
		// no histogram 
		if(query.getHistogram() == null) {
			histogramInfoLabel.setText("Without a codebook it is not possible to compute histograms and sort images by their histogram similarities.");
			rankList.clear();
			return;
		}
	
		// get the database
		List<ImageData> result = retrieve(query, imageDataList.values());
		
		// list all search results
		rankList.clear();
		if(histogramToggle.isSelected())
			rankList.addAll(result.stream().map(ImageData::getHistogram).map(this::drawHistogram).collect(Collectors.toList()));
		else
			rankList.addAll(result.stream().map(ImageData::getImage).collect(Collectors.toList()));
	}
	
	/**
	 * Visualize the histogram in an image
	 *  
	 * @param histogram
	 * @return
	 */
	protected Image drawHistogram(float[] histogram) {
		final int imageWidth = 256;
		final int imageHeight = 256;
		final WritableImage image = new WritableImage(imageWidth, imageHeight);
		
		final int histogrammBinCount = histogram.length;
		final int maxHistogramValue = IntStream.range(0, histogram.length).map(i -> (int)histogram[i]).sum();
		
		final int border = 2;
		final int binWidth = (imageWidth - 2*border) / histogrammBinCount;
		final int maxBinHeight = (imageHeight - 2*border);
		
		final int[] rgbs = new int[imageWidth * imageHeight];
		Arrays.fill(rgbs, 0xFF000000);
		for (int i = 0; i < histogrammBinCount; i++) {
			final int offsetX = 2 + binWidth * i;
			final int binHeight = (int) (maxBinHeight * (histogram[i] / maxHistogramValue));
			
			for (int y = 2 + (maxBinHeight - binHeight); y < imageHeight - 2; y++) 
				for (int x = offsetX; x < offsetX + binWidth; x++) 					
					rgbs[y * imageWidth + x] = 0xFFFFFFFF;
		}
		
		image.getPixelWriter().setPixels(0, 0, imageWidth, imageHeight, PixelFormat.getIntArgbInstance(), rgbs, 0, imageWidth);		
		return image;
	}

	/**
	 * Loads and displays the image of the image file.
	 */
	private void updateImageVisualization() {
		
		String imageFile = imageFileList.getSelectionModel().getSelectedItem();
		if(imageFile == null) {
			imagePane.setImage(null);
			gridImagePane.setImage(null);
			codeImagePane.setImage(null);
			recodedImagePane.setImage(null);
			globalRecodedImagePane.setImage(null);
			return;
		}
		
		// draw the image
		final ImageData imageData = this.imageDataList.get(imageFile);
		final Image image = imageData.getImage();
		imagePane.setImage(image);
		
		if(codebookTabPane.getSelectionModel().getSelectedIndex() == 0) {
			
			// draw the patches separated by a grid
			final int patchSize = 2 << patchSizeSlider.valueProperty().getValue().intValue();
			final List<byte[]> patches = extractPatches(image, patchSize);
			gridImagePane.setImage(resample(convertToImage(patches, true), gridImagePane.fitWidthProperty().intValue()));
			
			// draw the feature of the codebook
			final int codebookSize = codebookSizeSlider.valueProperty().getValue().intValue();
			final PixelCodebook codebook = buildCodebook(patches, codebookSize);			
			final List<byte[]> codes = IntStream.range(0, codebookSize).mapToObj(codebook::getCode).collect(Collectors.toList());
			codeImagePane.setImage(resample(convertToImage(codes, true), codeImagePane.fitWidthProperty().intValue()));
			codebookLabel.setText("Codebook ("+codebookSize+" times "+patchSize+"x"+patchSize+"px)");
			
			// replace the patches with the closest feature of the codebook and draw them
			final List<byte[]> recodedPatches = convertPatchToCodes(codebook, patches);
			recodedImagePane.setImage(resample(convertToImage(recodedPatches, false), recodedImagePane.fitWidthProperty().intValue()));
		}
		else {
			// draw the patches separated by a grid
			final int patchSize = 2 << globalPatchSizeSlider.valueProperty().getValue().intValue();
			final List<byte[]> patches = extractPatches(image, patchSize);
			gridImagePane.setImage(resample(convertToImage(patches, true), gridImagePane.fitWidthProperty().intValue()));
			
			if(globalCodebook != null) {
				final int codebookPatchSize = (int) Math.sqrt(globalCodebook.getCode(0).length / 3);
				final List<byte[]> codebookPatches = extractPatches(image, codebookPatchSize);
				
				// draw the feature of the codebook
				final int codebookSize = globalCodebookSizeSlider.valueProperty().getValue().intValue();
				final List<byte[]> codes = IntStream.range(0, codebookSize).mapToObj(globalCodebook::getCode).collect(Collectors.toList());
				globalCodeImagePane.setImage(resample(convertToImage(codes, true), globalCodeImagePane.fitWidthProperty().intValue()));
				
				// replace the patches with the closest feature of the codebook and draw them
				final List<byte[]> recodedPatches = convertPatchToCodes(globalCodebook, codebookPatches);
				globalRecodedImagePane.setImage(resample(convertToImage(recodedPatches, false), globalRecodedImagePane.fitWidthProperty().intValue()));
			}
		}
	}
	
	/**
	 * Draw all patches into an image
	 * 
	 * @param patches
	 * @param patchSize
	 * @param verticalPatches
	 * @param horizontalPatches
	 * @param drawGrid
	 * @return
	 */
	protected Image convertToImage(List<byte[]> patches, boolean drawGrid) {
		
		final int patchSize = (int) Math.sqrt(patches.get(0).length / 3);
		final int verticalPatches = (int) Math.ceil(Math.sqrt(patches.size()));
		final int horizontalPatches = verticalPatches;
		
		final int border = drawGrid ? 1 : 0;
		final int imageWidth = (patchSize + border) * horizontalPatches + border;
		final int imageHeight = (patchSize + border) * verticalPatches + border;

		final byte[] rgbs = new byte[imageWidth * imageHeight * 3];
		for (int patchY = 0; patchY < verticalPatches; patchY++) {
			for (int patchX = 0; patchX < horizontalPatches; patchX++) {
				final int patchIndex = patchY * horizontalPatches + patchX;
				if(patchIndex >= patches.size())
					break;
				
				final byte[] patch = patches.get(patchIndex);
				final int offsetY = patchY * (patchSize + border) + border;
				final int offsetX = patchX * (patchSize + border) + border;
				
				// Transfer the pixel data
				if(offsetX+patchSize <= imageWidth && offsetY+patchSize <= imageHeight) {
					for (int y = 0; y < patchSize; y++) {
						final int offset = ((offsetY + y) * imageWidth + offsetX) * 3;
						System.arraycopy(patch, y * patchSize * 3, rgbs, offset, patchSize * 3);
					}
				}
			}
		}
		
		final WritableImage image = new WritableImage(imageWidth, imageHeight);
		image.getPixelWriter().setPixels(0, 0, imageWidth, imageHeight, PixelFormat.getByteRgbInstance(), rgbs, 0, imageWidth*3);		
		return image;
	}
	
	  private Image resample(Image input, int targetSize) {
		    final int inputWidth = (int) input.getWidth();
		    final int inputHeight = (int) input.getHeight();
		    final int scale = targetSize / inputWidth;
		    
		    if(scale <= 1)
		    	return input;
		    
			final int[] inputPixels = new int[inputWidth * inputHeight];
			input.getPixelReader().getPixels(0, 0, inputWidth, inputHeight, WritablePixelFormat.getIntArgbInstance(), inputPixels, 0, inputWidth);
		    

			final int outputWidth = inputWidth * scale;
			final int outputHeight = inputHeight * scale;
		    final int[] outputPixel = new int[outputWidth * outputHeight];
		    for (int y = 0; y < inputHeight; y++) {
		      for (int x = 0; x < inputWidth; x++) {
		        final int argb = inputPixels[y * inputWidth + x];
		        
		        for (int dy = 0; dy < scale; dy++) {
		          for (int dx = 0; dx < scale; dx++) {
		        	  outputPixel[x * scale + dx + (y * scale + dy) * outputWidth] = argb;
		          }
		        }
		      }
		    }
		    
		    
		    WritableImage output = new WritableImage(outputWidth, outputHeight);
		    output.getPixelWriter().setPixels(0, 0, outputWidth, outputHeight, PixelFormat.getIntArgbInstance(), outputPixel, 0, outputWidth);
		    return output;
		  }

	/**
	 * Opens a dialog to select a data directory. All image files inside the
	 * directory will be filtered and categorized bases on their names. The
	 * resulting categories are listed in the category list view.
	 */
	private void buildCodebookAndComputeHistograms() {
		final int patchSize = 2 << globalPatchSizeSlider.valueProperty().getValue().intValue();
		final int histogramBinCount = globalCodebookSizeSlider.valueProperty().getValue().intValue();
		
		// close old codebook building thread
		if(codebookThread != null && codebookThread.isAlive()) {
			Thread cancelThread = new Thread(() -> {
				try {
					codebookBuildTimer.cancel();
					Platform.runLater(() -> {
						histogramInfoLabel.setText("Please wait old codebook is building.");
					});
					codebookThread.interrupt();
					codebookThread.join();
					Platform.runLater(() -> {
						histogramInfoLabel.setText("Old codebook finished.");
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			cancelThread.start();
		}
		else {
			
			// run the building process in the background thread
			histogramInfoLabel.setText("Build codebook");
			codebookThread = new Thread(() -> {
				
				// update the ui every second
				AtomicInteger ai = new AtomicInteger(0);
				TimerTask timerTask = new TimerTask() {			
					@Override
					public void run() {
						Platform.runLater(() -> {
							histogramInfoLabel.setText("Building codebook for "+ai.incrementAndGet()+" secs");
						});
					}
				};
				codebookBuildTimer = new Timer();
				codebookBuildTimer.scheduleAtFixedRate(timerTask, 1000, 1000);
				
				// collect patches 
				final List<byte[]> allPatches = new ArrayList<>();
				for(ImageData imageData :  imageDataList.values()) 
					allPatches.addAll(extractPatches(imageData.getImage(), patchSize));
				
				// create the codebook 
				globalCodebook = buildCodebook(allPatches, histogramBinCount);					
				codebookBuildTimer.cancel();
				
				// and compute a histogram for every image
				for(ImageData imageData : imageDataList.values()) 
					computeHistogram(globalCodebook, imageData, patchSize);

				Platform.runLater(() -> {
					globalCodebookLabel.setText("Codebook ("+histogramBinCount+" times "+patchSize+"x"+patchSize+"px)");
					histogramInfoLabel.setText("Codebook complete. Double click a filename to start ranking images by similarity.");
					updateImageVisualization();
				});
				
			});
			codebookThread.start();
		}		
	}	
	
	
	
	/**
	 * Splits the image into several sub regions. The regions have a size of patchSize x patchSize.
	 * All the patches will be listed in scan line order into an array list. Each entry in the list 
	 * contains all the pixels of this sub region and their corresponding RGB pixels. There 
	 * are patchSize x patchSize x RGB (e.g. 16x16x3=768) values in a patch and every one of 
	 * them has a value between 0 and 255. The values are sorted in signed bytes:
	 * 
	 * int i = 200;				// prints 200
	 * byte b = (byte) 200;		// prints -256
	 * int r = b & 0xFF;		// prints 200
	 * 
	 * A similar technique is used to read and write ARGB values from and into an integer.
	 * 
	 * Example of a 6x4 pixel images divided into 3x2 sub regions with a patch size of 2.
	 * -------------------------------
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * -------------------------------
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * | RGB RGB | RGB RGB | RGB RGB |
	 * -------------------------------
	 * 
	 * @param image
	 * @param patchSize
	 * @return list of patches
	 */
	public abstract List<byte[]> extractPatches(Image image, int patchSize);
	
	/**
	 * Creates a codebook by taking the pixel of a patch as a feature vector
	 * and clustering all provided feature vectors into a few clusters.
	 * The amount of cluster is determined by the codebook size.
	 * 
	 * @param patches
	 * @param codebookSize
	 * @return codebook
	 */
	public abstract PixelCodebook buildCodebook(List<byte[]> patches, int codebookSize);
	
	/**
	 * Converts the feature vectors of a patch into the feature vector of the 
	 * nearest cluster center from the codebook.
	 * 
	 * @param codebook
	 * @param patches
	 * @return
	 */
	public abstract List<byte[]> convertPatchToCodes(PixelCodebook codebook, List<byte[]> patches);
	
	
	/**
	 * Computes the corresponding histogram of the image using the codebook.
	 * First it splits the image into smaller sub-regions (patches).
	 * Then it uses the patches and the codebook to create a histogram of occurrences.
	 * The histogram is stored in the imageData
	 * 
	 * @param codebook
	 * @param imageData
	 * @param patchSize
	 */
	public abstract void computeHistogram(PixelCodebook codebook, ImageData imageData, int patchSize);
	
	
	/**
	 * Sorts the elements in the database based on the euclidean distance to the search query.
	 * The distance will be calculated between the histogram of the query and the histogram
	 * of every database entry. A sorted list of all database entries will be returned.
	 * 
	 * @param query
	 * @param database
	 * @return sorted database list
	 */
	public abstract List<ImageData> retrieve(ImageData query, Collection<ImageData> database);
}
