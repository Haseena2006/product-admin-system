package com.example.productadmin.controller.web;

import com.example.productadmin.dto.ProductRequest;
import com.example.productadmin.dto.ProductResponse;
import com.example.productadmin.exception.DuplicateProductException;
import com.example.productadmin.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class ProductWebController {

    private static final int PAGE_SIZE = 8;

    private final ProductService productService;

    public ProductWebController(ProductService productService) {
        this.productService = productService;
    }

    @ModelAttribute("categories")
    public List<String> categories() {
        return productService.categories();
    }

    @GetMapping("/products")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<ProductResponse> result = productService.search(q, category, sort, dir, page, PAGE_SIZE);

        model.addAttribute("page", result);
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("startPage", Math.max(0, result.getNumber() - 2));
        model.addAttribute("endPage", Math.max(0, Math.min(result.getTotalPages() - 1, result.getNumber() + 2)));
        return "product-list";
    }

    @GetMapping("/products/new")
    public String newForm(Model model) {
        model.addAttribute("product", new ProductRequest());
        model.addAttribute("productId", null);
        return "product-form";
    }

    @PostMapping("/products")
    public String create(@Valid @ModelAttribute("product") ProductRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute("productId", null);
        if (bindingResult.hasErrors()) {
            return "product-form";
        }
        try {
            productService.create(request);
        } catch (DuplicateProductException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            return "product-form";
        }
        redirectAttributes.addFlashAttribute("success", "Product created successfully.");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        return "product-details";
    }

    @GetMapping("/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getForEdit(id));
        model.addAttribute("productId", id);
        return "product-form";
    }

    @PostMapping("/products/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("product") ProductRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        model.addAttribute("productId", id);
        if (bindingResult.hasErrors()) {
            return "product-form";
        }
        try {
            productService.update(id, request);
        } catch (DuplicateProductException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            return "product-form";
        }
        redirectAttributes.addFlashAttribute("success", "Product updated successfully.");
        return "redirect:/products/" + id;
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Product deleted successfully.");
        return "redirect:/products";
    }
}
