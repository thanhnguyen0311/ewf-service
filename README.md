# EWF Service Documentation

## Project Overview

This project, `ewf-service`, is a Spring-based service application that provides a range of functionalities for managing different aspects of business operations. The application follows a layered architecture with repositories, services, and controllers to ensure maintainable and scalable code.

## Core Services

### 1. LPN Service (License Plate Number)

**Implementation Class:** `LpnServiceImpl`  
**Package:** `com.danny.ewf_service.service.impl`

Manages license plate numbers with the following operations:
- Creating new LPNs
- Updating existing LPNs
- Retrieving all LPNs

**Key Methods:**
- `newLpn(LpnRequestDto)`: Creates a new LPN
- `updateLpn(LpnEditRequestDto)`: Updates an existing LPN
- `getAllLpn()`: Retrieves all LPNs as DTOs

**Dependencies:**
- `ILpnMapper`
- `ComponentRepository`
- `BayLocationRepository`
- `CustomUserDetailsService`
- `LogService`
- `LpnRepository`

---

### 2. Inventory Service

**Implementation Class:** `InventoryServiceImpl`  
**Package:** `com.danny.ewf_service.service.impl`

Manages inventory-related operations including:
- Retrieving product inventory information
- Managing component inventory
- Updating component details

**Key Methods:**
- `inventoryProductListByQuantityASC()`: Lists products sorted by quantity
- `inventoryProductAll()`: Retrieves all product inventories
- `getInventoryProductCountById(Long)`: Gets inventory count for a specific product
- `findAllComponentsInventory()`: Finds all component inventories
- `updateComponent(ComponentInventoryRequestDto)`: Updates component information

**Dependencies:**
- `ProductComponentRepository`
- `IComponentMapper`
- `ConfigurationRepository`
- `CustomUserDetailsService`
- `ComponentRepository`
- `ComponentService`

---

### 3. Order Service

**Implementation Class:** `OrderServiceImpl`  
**Package:** `com.danny.ewf_service.service.impl`

Manages order operations with the following functionality:
- Paginated retrieval of orders with sorting capabilities

**Key Methods:**
- `getOrdersByPageAndSort(int)`: Retrieves paginated orders sorted by order date in descending order

**Dependencies:**
- `OrderRepository`

---

### 4. Product Service

**Implementation Class:** `ProductServiceImpl`  
**Package:** `com.danny.ewf_service.service.impl`

Handles product-related operations:
- Finding products by SKU
- Managing product details
- Calculating product pricing
- Handling product images

**Key Methods:**
- `findBySku(String)`: Finds a product by its SKU
- `findAllProductsToDtos()`: Converts all products to DTOs
- `getAllProductsSearch()`: Gets all products for search functionality
- `updateProductDetailById(Long, ProductDetailRequestDto)`: Updates product details
- `calculateEWFDirectPriceGround(Product, List<String[]>)`: Calculates ground shipping price
- `findMergedProducts(Product)`: Finds merged products

**Dependencies:**
- `SKUGenerator`
- `IProductMapper`
- `ProductRepository`
- `ComponentService`
- `ProductComponentRepository`
- `CacheService`
- `InventoryService`
- `ImageService`

---

### 5. Role Service

**Implementation Class:** `RoleServiceImpl`  
**Package:** `com.danny.ewf_service.service.impl`

Manages role operations:
- Retrieving all available roles

**Key Methods:**
- `findAll()`: Retrieves all roles as DTOs

**Dependencies:**
- `RoleRepository`
- `IRoleMapper`

---

### 6. Authentication Service

**Implementation Class:** `AuthServiceImpl`  
**Package:** `com.danny.ewf_service.service.auth`

Handles user authentication and registration:
- User registration
- User information retrieval

**Key Methods:**
- `register(RegisterRequest)`: Registers a new user
- `getInfo()`: Gets information about the authenticated user

**Dependencies:**
- `UserRepository`
- `RoleRepository`
- `PasswordEncoder`

---

### 7. Customer Service

**Implementation Class:** `CustomerServiceImpl`  
**Package:** `com.danny.ewf_service.service.impl`

Manages customer-related operations:
- Searching customers by phone number

**Key Methods:**
- `findCustomersByPartialPhone(String)`: Searches for customers by partial phone number

**Dependencies:**
- `CustomerRepository`
- `ICustomerMapper`

## Architecture Overview

The application follows a standard Spring service architecture:

1. **Repositories** - Interface with the database using Spring Data JPA
2. **Services** - Implement business logic and interface with repositories
3. **DTOs** - Transfer objects for API communication
4. **Mappers** - Convert between entities and DTOs
5. **Controllers** - Handle HTTP requests and responses (not detailed in this README)

## Dependencies

The project utilizes several key technologies:
- Spring Boot
- Spring Data JPA
- Spring Security
- Lombok
- Java 23

## Getting Started

To run this application:

1. Ensure you have Java 23 installed
2. Clone the repository
3. Build using Maven: `mvn clean install`
4. Run the application: `java -jar target/ewf-service.jar`

## Contributing

When contributing to this project:
1. Follow the existing code structure and naming conventions
2. Write unit tests for new functionality
3. Update documentation for any modified services
4. Submit pull requests for review

## Contact

For questions or support, please contact the development team at: **nct031194@icloud.com**
