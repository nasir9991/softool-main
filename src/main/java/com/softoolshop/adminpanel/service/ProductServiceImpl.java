package com.softoolshop.adminpanel.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.softoolshop.adminpanel.dto.ProductDTO;
import com.softoolshop.adminpanel.dto.ProductFilterDTO;
import com.softoolshop.adminpanel.entity.Product;
import com.softoolshop.adminpanel.entity.ProductDesc;
import com.softoolshop.adminpanel.repository.ProductDescRepository;
import com.softoolshop.adminpanel.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private ProductRepository prodRepo;
	@Autowired
	private ProductDescRepository prodDescRepo;

	@Override
	public List<Product> createProducts(List<ProductDTO> products) {
		List<Product> entities = Arrays.asList(modelMapper.map(products, Product[].class));
		return prodRepo.saveAll(entities);
	}

	@Override
	public List<ProductDTO> getAllProducts() {
		List<ProductDTO> entities = prodRepo.getActiveProducts(0);
		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().scheme("https").replacePath(null).build()
				.toUriString();

		for (ProductDTO dto : entities) {
			// Strip ₹ and parse prices
			double oldPrice = parsePrice(dto.getOldPriceStr());
			double newPrice = parsePrice(dto.getPriceStr());
			// Calculate discount percentage
			if (oldPrice > 0) {
				double discountPercent = ((oldPrice - newPrice) / oldPrice) * 100;
				dto.setDiscount(String.format("-%.0f%%", discountPercent));
			} else {
				dto.setDiscount("-0%");
			}
			dto.setPriceStr(this.getCurrSymbol() + dto.getPriceStr());
			dto.setOldPriceStr(this.getCurrSymbol() + dto.getOldPriceStr());
			dto.setImageUrl(baseUrl + "/softools/api/images/product/" + dto.getImageUrl());
			dto.setProductLink(baseUrl + "/softools/api/products/itm/" + dto.getProductId());
		}

		return entities;

//		return entities.stream().map(entity -> {
//			ProductDTO dto = modelMapper.map(entity, ProductDTO.class);
//
//			// Strip ₹ and parse prices
//			double oldPrice = parsePrice(dto.getOldPriceStr());
//			double newPrice = parsePrice(dto.getPriceStr());
//
//			// Calculate discount percentage
//			if (oldPrice > 0) {
//				double discountPercent = ((oldPrice - newPrice) / oldPrice) * 100;
//				dto.setDiscount(String.format("-%.0f%%", discountPercent));
//			} else {
//				dto.setDiscount("-0%");
//			}
//			dto.setImageUrl(baseUrl + "/softools/api/images/product/" + dto.getImageUrl());
//			dto.setProductLink(baseUrl + "/softools/api/products/itm/" + dto.getProductId());
//			return dto;
//		}).collect(Collectors.toList());
	}

	private double parsePrice(String priceStr) {
		if (priceStr == null || priceStr.isEmpty())
			return 0;
		// System.out.println("======="+priceStr);
		return Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
	}

	@Override
	public Product addProduct(ProductDTO product) {
		Product entity = modelMapper.map(product, Product.class);
		return prodRepo.save(entity);
	}

	@Override
	public ProductDTO getProductById(Integer productId) {
		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().scheme("https").replacePath(null).build()
				.toUriString();
		Optional<Product> opt = prodRepo.findById(productId);
		ProductDTO prodDto = null;
		if (opt.isPresent()) {
			prodDto = modelMapper.map(opt.get(), ProductDTO.class);
		}
		double dscntAmt = Double.parseDouble(prodDto.getOldPriceStr()) - Double.parseDouble(prodDto.getPriceStr());
		double dscntPer = dscntAmt/Double.parseDouble(prodDto.getOldPriceStr())*100.00;
		prodDto.setDiscount(String.valueOf(dscntPer));
		prodDto.setPriceStr(this.getCurrSymbol() + prodDto.getPriceStr());
		prodDto.setOldPriceStr(this.getCurrSymbol() + prodDto.getOldPriceStr());
		prodDto.setProductLink(baseUrl + "/softools/api/products/itm/" + prodDto.getProductId());
		prodDto.setImageUrl(baseUrl + "/softools/api/images/product/" + prodDto.getImageUrl());
		// prodDto.setDescription(prodDescRepo.getProductDescById(prodDto.getProductId()));
		return prodDto;
	}

	@Override
	public ProductDesc addProdDescription(ProductDTO product) {
		ProductDesc entity = new ProductDesc();
		entity.setProductId(product.getProductId());
		entity.setDescription(product.getDescription());
		entity.setShortDesc(product.getTitle());
		return prodDescRepo.save(entity);
	}

//	@Override
//	public List<ProductDTO> getProductsByCategory(Integer categoryId) {
//		List<Product> entities = prodRepo.findByCategoryId(categoryId);
//		return entities.stream().map(entity -> {
//			ProductDTO dto = modelMapper.map(entity, ProductDTO.class);
//
//			// Strip ₹ and parse prices
//			double oldPrice = parsePrice(dto.getOldPriceStr());
//			double newPrice = parsePrice(dto.getPriceStr());
//
//			// Calculate discount percentage
//			if (oldPrice > 0) {
//				double discountPercent = ((oldPrice - newPrice) / oldPrice) * 100;
//				dto.setDiscount(String.format("-%.0f%%", discountPercent));
//			} else {
//				dto.setDiscount("-0%");
//			}
//			dto.setImageUrl(baseUrl + "/softools/api/images/product/" + dto.getImageUrl());
//			dto.setProductLink(baseUrl + "/softools/api/products/itm/" + dto.getProductId());
//			return dto;
//		}).collect(Collectors.toList());
//	}

	@Override
	public List<ProductDTO> getFilteredProducts(ProductFilterDTO filterRequest) {
		List<Product> entities = prodRepo.getFilteredProducts(filterRequest);

		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().scheme("https").replacePath(null).build()
				.toUriString();

		return entities.stream().map(entity -> {
			ProductDTO dto = modelMapper.map(entity, ProductDTO.class);

			// Strip ₹ and parse prices
			double oldPrice = parsePrice(dto.getOldPriceStr());
			double newPrice = parsePrice(dto.getPriceStr());

			// Calculate discount percentage
			if (oldPrice > 0) {
				double discountPercent = ((oldPrice - newPrice) / oldPrice) * 100;
				dto.setDiscount(String.format("-%.0f%%", discountPercent));
			} else {
				dto.setDiscount("-0%");
			}
			dto.setPriceStr(this.getCurrSymbol() + dto.getPriceStr());
			dto.setOldPriceStr(this.getCurrSymbol() + dto.getOldPriceStr());
			dto.setImageUrl(baseUrl + "/softools/api/images/product/" + dto.getImageUrl());
			dto.setProductLink(baseUrl + "/softools/api/products/itm/" + dto.getProductId());
			return dto;
		}).collect(Collectors.toList());
	}

	@Override
	public ProductDTO getProductDescriptionById(Integer productId) {

		return prodRepo.findById(productId).map(product -> modelMapper.map(product, ProductDTO.class)).orElse(null);
	}

	@Override
	public Product updateProd(ProductDTO product) {

		// product.setPriceStr(this.formatPriceWithRupee(String.valueOf(product.getNumericPrice())));
		// product.setOldPriceStr(this.formatPriceWithRupee(product.getOldPriceStr()));

		Optional<Product> opt = prodRepo.findById(product.getProductId());
		if (opt.isPresent()) {
			Product entityFrmDb = opt.get();
			entityFrmDb.setTitle(product.getTitle());
			entityFrmDb.setCategoryId(product.getCategoryId());
			entityFrmDb.setInactive(product.getInactive());
			entityFrmDb.setDescription(product.getDescription());
			if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
				entityFrmDb.setImageUrl(this.extractFileName(product.getImageUrl()));
			}
			entityFrmDb.setProductLink(null);
			entityFrmDb.setNumericPrice(product.getNumericPrice());
			entityFrmDb.setPriceStr(product.getPriceStr());
			entityFrmDb.setOldPriceStr(product.getOldPriceStr());
			return prodRepo.save(entityFrmDb);
		} else {
			Product entity = modelMapper.map(product, Product.class);
			return prodRepo.save(entity);
		}

	}

//	private String formatPriceWithRupee(String priceStr) {
//		String rupee = this.getCurrSymbol();
//		if (priceStr == null || priceStr.isBlank()) {
//			return rupee + "0"; // default fallback
//		}
//		return priceStr.startsWith(rupee) ? priceStr : rupee + priceStr;
//	}

	private String extractFileName(String imageUrl) {
		return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
	}

	private String getCurrSymbol() {
		java.util.Currency curr_ency = java.util.Currency.getInstance("INR");
		// Getting the symbol of the currency
		String currSymbol = curr_ency.getSymbol();
		// System.out.println("Symbol for the currency of India is: " + currSymbol);
		return currSymbol;
	}

}
