package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {

	@GetMapping({ "/products", "/ajax" })
	public String ajaxPage() {
		return "ajax";
	}
}