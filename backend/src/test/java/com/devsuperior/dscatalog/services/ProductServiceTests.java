package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;
	
	@Mock
	private ProductRepository repositoryProduct;
	
	//@Mock
//	private CategoryRepository repositoryCategory;
	
	private long existingId;
	private long nonExistingId;
	private long dependedId;
	private PageImpl<Product> page;
	private Product product;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependedId = 3L;
		product = Factory.createProduct();
		page = new PageImpl<>(List.of(product));

		Mockito.when(repositoryProduct.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
		
		Mockito.when(repositoryProduct.save(ArgumentMatchers.any())).thenReturn(product);
		
		Mockito.when(repositoryProduct.findById(existingId)).thenReturn(Optional.of(product));
		Mockito.when(repositoryProduct.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());
		

		Mockito.when(repositoryProduct.save(ArgumentMatchers.any())).thenReturn(product);
		
		Mockito.doNothing().when(repositoryProduct).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repositoryProduct).deleteById(nonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repositoryProduct).deleteById(dependedId);
	}
	
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExist() {
		
		Assertions.assertDoesNotThrow(() -> {
			ProductDTO result = service.findById(existingId);
		});

		Mockito.verify(repositoryProduct, Mockito.times(1)).findById(existingId);
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
		Mockito.verify(repositoryProduct).findById(nonExistingId);
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		
		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductDTO> result = service.findAllPaged(pageable);
		Assertions.assertNotNull(result);
		Mockito.verify(repositoryProduct).findAll(pageable);
		
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);		
		});
		Mockito.verify(repositoryProduct, Mockito.times(1)).deleteById(existingId);
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);		
		});
		Mockito.verify(repositoryProduct, Mockito.times(1)).deleteById(nonExistingId);
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependedId() {
		
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependedId);		
		});
		Mockito.verify(repositoryProduct, Mockito.times(1)).deleteById(dependedId);
	}
	
	
}
