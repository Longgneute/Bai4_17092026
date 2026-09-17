package vn.iotstar.controller.api;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.IStorageService;

@RestController
@RequestMapping("/api/product")
public class ProductAPIController {

    private final IProductService productService;
    private final ICategoryService categoryService;
    private final IStorageService storageService;

    public ProductAPIController(
            IProductService productService,
            ICategoryService categoryService,
            IStorageService storageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<Response> getAllProduct(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (page < 0) page = 0;
        if (size < 1) size = 5;

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "productId"));

        Page<Product> result;

        if (keyword.trim().isEmpty()) {
            result = productService.findAll(pageable);
        } else {
            result = productService.findByProductNameContaining(
                    keyword.trim(), pageable);
        }

        return ResponseEntity.ok(
                new Response(true, "Lấy Product thành công", result));
    }

    @PostMapping("/getProduct")
    public ResponseEntity<Response> getProduct(
            @RequestParam Long productId) {

        Optional<Product> optional = productService.findById(productId);

        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(false, "Không tìm thấy Product", null));
        }

        return ResponseEntity.ok(
                new Response(true, "Thành công", optional.get()));
    }

    @PostMapping("/addProduct")
    public ResponseEntity<Response> addProduct(
            @RequestParam String productName,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam double unitPrice,
            @RequestParam double discount,
            @RequestParam String description,
            @RequestParam Long categoryId,
            @RequestParam int quantity,
            @RequestParam short status) {

        Optional<Product> existed =
                productService.findByProductName(productName.trim());

        if (existed.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(false,
                            "Sản phẩm này đã tồn tại trong hệ thống",
                            null));
        }

        Optional<Category> category =
                categoryService.findById(categoryId);

        if (category.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(false,
                            "Không tìm thấy Category",
                            null));
        }

        try {
            Product product = new Product();

            product.setProductName(productName.trim());
            product.setUnitPrice(unitPrice);
            product.setDiscount(discount);
            product.setDescription(description);
            product.setQuantity(quantity);
            product.setStatus(status);
            product.setCreateDate(new Date());
            product.setCategory(category.get());

            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = storageService.getSorageFilename(
                        imageFile, UUID.randomUUID().toString());

                storageService.store(imageFile, filename);
                product.setImages(filename);
            }

            Product saved = productService.save(product);

            return ResponseEntity.ok(
                    new Response(true, "Thêm Product thành công", saved));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false,
                            "Lỗi khi thêm Product",
                            null));
        }
    }

    @PutMapping("/updateProduct")
    public ResponseEntity<Response> updateProduct(
            @RequestParam Long productId,
            @RequestParam String productName,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam double unitPrice,
            @RequestParam double discount,
            @RequestParam String description,
            @RequestParam Long categoryId,
            @RequestParam int quantity,
            @RequestParam short status) {

        Optional<Product> optional =
                productService.findById(productId);

        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(false,
                            "Không tìm thấy Product", null));
        }

        Optional<Category> category =
                categoryService.findById(categoryId);

        if (category.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(false,
                            "Không tìm thấy Category", null));
        }

        try {
            Product product = optional.get();

            product.setProductName(productName.trim());
            product.setUnitPrice(unitPrice);
            product.setDiscount(discount);
            product.setDescription(description);
            product.setQuantity(quantity);
            product.setStatus(status);
            product.setCategory(category.get());

            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = storageService.getSorageFilename(
                        imageFile, UUID.randomUUID().toString());

                storageService.store(imageFile, filename);
                product.setImages(filename);
            }

            Product saved = productService.save(product);

            return ResponseEntity.ok(
                    new Response(true,
                            "Cập nhật Product thành công",
                            saved));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false,
                            "Lỗi khi cập nhật Product",
                            null));
        }
    }

    @DeleteMapping("/deleteProduct")
    public ResponseEntity<Response> deleteProduct(
            @RequestParam Long productId) {

        Optional<Product> optional =
                productService.findById(productId);

        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response(false,
                            "Không tìm thấy Product", null));
        }

        try {
            Product product = optional.get();
            productService.delete(product);

            return ResponseEntity.ok(
                    new Response(true,
                            "Xóa Product thành công",
                            null));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false,
                            "Không thể xóa Product",
                            null));
        }
    }
}
