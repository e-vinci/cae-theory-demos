package be.vinci.ipl.cae.demo.services;

import be.vinci.ipl.cae.demo.models.dtos.NewPizza;
import be.vinci.ipl.cae.demo.models.entities.Pizza;
import be.vinci.ipl.cae.demo.repositories.PizzaRepository;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Pizza service.
 */
@Service
public class PizzaService {

  private final PizzaRepository pizzaRepository;
  private final String blobServiceEndpoint;
  private final String sasToken;
  private final String containerName;

  /**
   * Constructor.
   *
   * @param pizzaRepository the pizza repository
   */
  public PizzaService(PizzaRepository pizzaRepository,
      @Value("${azure.blob.service-endpoint}") String blobServiceEndpoint,
      @Value("${azure.blob.sas-token}") String sasToken,
      @Value("${azure.blob.container-name}") String containerName) {
    this.pizzaRepository = pizzaRepository;
    this.blobServiceEndpoint = blobServiceEndpoint;
    this.sasToken = sasToken;
    this.containerName = containerName;
  }

  /**
   * Read all pizzas.
   *
   * @param order if order contains "title" the pizzas will be ordered by title in ascending order
   *              if order contains "-title" the pizzas will be ordered by title in descending
   *              order.
   * @return the pizzas
   */
  public Iterable<Pizza> readAllPizzas(String order) {
    if (order == null || !order.contains("title")) {
      return pizzaRepository.findAll();
    }

    if (order.contains("-")) {
      return pizzaRepository.findAllByOrderByTitleDesc();
    }
    return pizzaRepository.findAllByOrderByTitleAsc();
  }

  /**
   * Read a pizza by its id.
   *
   * @param id the id
   * @return the pizza
   */
  public Pizza readPizzaById(Long id) {
    return pizzaRepository.findById(id).orElse(null);
  }

  /**
   * Create a pizza.
   *
   * @param newPizza the new pizza
   * @param image    the image
   * @return the pizza
   */
  @Transactional
  public Pizza createPizza(NewPizza newPizza, MultipartFile image) {
    Pizza pizza = new Pizza();
    pizza.setTitle(newPizza.getTitle());
    pizza.setContent(newPizza.getContent());

    if (image != null && !image.isEmpty()) {
      String blobUrl = uploadImageToBlob(image);
      pizza.setImageLocation(blobUrl);
    }

    return pizzaRepository.save(pizza);
  }

  /**
   * Upload an image to a blob on Azure storage with a generated UUID as filename. The image is
   * uploaded to the "images" folder. The original filename is stored in the metadata.
   *
   * @param image
   * @return the blob url
   * @throws IOException
   */
  private String uploadImageToBlob(MultipartFile image) {
    String imageUUID = UUID.randomUUID().toString();
    Map<String, String> metadata = new HashMap<>();

    String blobName = imageUUID;

    metadata.put("originalFileName", image.getOriginalFilename());
    BlobClientBuilder blobClientBuilder = new BlobClientBuilder()
        .endpoint(blobServiceEndpoint)
        .sasToken(sasToken)
        .containerName(containerName)
        .blobName(blobName);

    try {
      blobClientBuilder.buildClient().upload(image.getInputStream(), image.getSize(), true);
      blobClientBuilder.buildClient().setMetadata(metadata);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    blobClientBuilder.buildClient()
        .setHttpHeaders(new BlobHttpHeaders().setContentType(image.getContentType()));

    return blobClientBuilder.buildClient().getBlobUrl();
  }


  /**
   * Delete a pizza by its id.
   *
   * @param id the id
   * @return the pizza
   */
  @Transactional
  public Pizza deletePizza(Long id) {
    Pizza pizza = pizzaRepository.findById(id).orElse(null);
    if (pizza != null) {
      if (pizza.getImageLocation() != null) {
        deleteImageFromBlob(pizza.getImageLocation());
      }
      pizzaRepository.delete(pizza);
    }
    return pizza;
  }

  /**
   * Delete an image from the blob storage.
   *
   * @param imageLocation the image location
   */
  private void deleteImageFromBlob(String imageLocation) {
    if (imageLocation != null && !imageLocation.isEmpty() && imageLocation.contains(
        blobServiceEndpoint)) {
      String blobName = imageLocation.substring(imageLocation.lastIndexOf("/") + 1);
      BlobClientBuilder blobClientBuilder = new BlobClientBuilder()
          .endpoint(blobServiceEndpoint)
          .sasToken(sasToken)
          .containerName(containerName)
          .blobName(blobName);

      blobClientBuilder.buildClient().delete();
    }
  }

  /**
   * Update a pizza.
   *
   * @param id       the id
   * @param newPizza the new pizza
   * @return the pizza
   */
  @Transactional
  public Pizza updatePizza(Long id, NewPizza newPizza) {
    Pizza pizza = pizzaRepository.findById(id).orElse(null);
    if (pizza == null) {
      return null;
    }

    if (newPizza.getTitle() != null) {
      pizza.setTitle(newPizza.getTitle());
    }
    if (newPizza.getContent() != null) {
      pizza.setContent(newPizza.getContent());
    }

    return pizzaRepository.save(pizza);
  }

}
