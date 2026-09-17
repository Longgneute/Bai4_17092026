package vn.iotstar.controller.api;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.entity.Category;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IStorageService;

@RestController
@RequestMapping("/api/category")
public class CategoryAPIController {

	private ICategoryService categoryService;

	private IStorageService storageService;

	public CategoryAPIController(ICategoryService categoryService, IStorageService storageService) {

		this.categoryService = categoryService;
		this.storageService = storageService;
	}

	// =========================
	// GET ALL
	// =========================

	@GetMapping
	public ResponseEntity<?> getAllCategory() {

		return new ResponseEntity<Response>(new Response(true, "Thành công", categoryService.findAll()), HttpStatus.OK);
	}

	// =========================
	// GET BY ID
	// =========================

	@PostMapping("/getCategory")
	public ResponseEntity<?> getCategory(@Validated @RequestParam("id") Long id) {

		Optional<Category> category = categoryService.findById(id);

		if (category.isPresent()) {

			return new ResponseEntity<Response>(new Response(true, "Thành công", category.get()), HttpStatus.OK);

		} else {

			return new ResponseEntity<Response>(new Response(false, "Thất bại", null), HttpStatus.NOT_FOUND);
		}
	}

	// =========================
	// ADD
	// =========================

	@PostMapping("/addCategory")
	public ResponseEntity<?> addCategory(

			@Validated @RequestParam("categoryName") String categoryName,

			@RequestParam(value = "icon", required = false) MultipartFile icon) {

		Optional<Category> optCategory = categoryService.findByCategoryName(categoryName);

		if (optCategory.isPresent()) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(false, "Category đã tồn tại trong hệ thống", null));
		}

		Category category = new Category();

		if (icon != null && !icon.isEmpty()) {

			UUID uuid = UUID.randomUUID();

			String uuidString = uuid.toString();

			String filename = storageService.getSorageFilename(icon, uuidString);

			category.setIcon(filename);

			storageService.store(icon, filename);
		}

		category.setCategoryName(categoryName);

		categoryService.save(category);

		return new ResponseEntity<Response>(new Response(true, "Thêm Thành công", category), HttpStatus.OK);
	}

	// =========================
	// UPDATE
	// =========================

	@PutMapping("/updateCategory")
	public ResponseEntity<?> updateCategory(

			@Validated @RequestParam("categoryId") Long categoryId,

			@Validated @RequestParam("categoryName") String categoryName,

			@RequestParam(value = "icon", required = false) MultipartFile icon) {

		Optional<Category> optCategory = categoryService.findById(categoryId);

		if (optCategory.isEmpty()) {

			return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Category", null),
					HttpStatus.BAD_REQUEST);
		}

		Category category = optCategory.get();

		// Nếu upload ảnh mới
		if (icon != null && !icon.isEmpty()) {

			UUID uuid = UUID.randomUUID();

			String uuidString = uuid.toString();

			String filename = storageService.getSorageFilename(icon, uuidString);

			category.setIcon(filename);

			storageService.store(icon, filename);
		}

		category.setCategoryName(categoryName);

		categoryService.save(category);

		return new ResponseEntity<Response>(new Response(true, "Cập nhật Thành công", category), HttpStatus.OK);
	}

	// =========================
	// DELETE
	// =========================

	@DeleteMapping("/deleteCategory")
	public ResponseEntity<?> deleteCategory(

			@Validated @RequestParam("categoryId") Long categoryId) {

		Optional<Category> optCategory = categoryService.findById(categoryId);

		if (optCategory.isEmpty()) {

			return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Category", null),
					HttpStatus.BAD_REQUEST);
		}

		Category category = optCategory.get();

		categoryService.delete(category);

		return new ResponseEntity<Response>(new Response(true, "Xóa Thành công", category), HttpStatus.OK);
	}
}