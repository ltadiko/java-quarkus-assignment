# API Collections

This folder contains pre-configured API collections for testing the Fulfilment Cost Control API.

## 📬 Postman

### Import

1. Open Postman
2. Click **Import** → **Upload Files**
3. Select `postman/Fulfilment_Cost_Control_API.postman_collection.json`

### Configuration

The collection includes a `baseUrl` variable:
- Default: `http://localhost:8080`
- To change: Edit Collection → Variables → Update `baseUrl`

### Requests Included

| Folder | Requests |
|--------|----------|
| Warehouses | List All, Get by ID, Create, Replace, Archive |
| Stores | List All, Get by ID, Create, Update, Patch, Delete |
| Products | List All, Get by ID, Create, Update, Delete |
| Fulfillment Assignments | List All, Get by ID/Store/Warehouse/Product, Create, Delete |

---

## 🐻 Bruno

[Bruno](https://www.usebruno.com/) is a fast, open-source, Git-friendly API client.

### Import

1. Open Bruno
2. Click **Open Collection**
3. Navigate to and select the `bruno/` folder

### Structure

```
bruno/
├── bruno.json                    # Collection metadata
├── environments/
│   └── local.bru                # Local environment (baseUrl: http://localhost:8080)
├── warehouses/
│   ├── list-all-warehouses.bru
│   ├── get-warehouse-by-id.bru
│   ├── create-warehouse.bru
│   ├── replace-warehouse.bru
│   └── archive-warehouse.bru
├── stores/
│   ├── list-all-stores.bru
│   ├── get-store-by-id.bru
│   ├── create-store.bru
│   ├── update-store.bru
│   └── delete-store.bru
├── products/
│   ├── list-all-products.bru
│   ├── get-product-by-id.bru
│   ├── create-product.bru
│   ├── update-product.bru
│   └── delete-product.bru
└── fulfillment-assignments/
    ├── list-all-assignments.bru
    ├── get-assignment-by-id.bru
    ├── get-assignments-by-store.bru
    ├── get-assignments-by-warehouse.bru
    ├── get-assignments-by-product.bru
    ├── create-assignment.bru
    └── delete-assignment.bru
```

### Environment Variables

Edit `environments/local.bru` to change the base URL:

```
vars {
  baseUrl: http://localhost:8080
}
```

---

## 🚀 Quick Start

1. Start the application:
   ```bash
   cd java-assignment
   docker-compose up -d
   ```

2. Import the collection into your preferred client (Postman or Bruno)

3. Start testing the APIs!

---

## 📡 API Base URL

| Environment | URL |
|-------------|-----|
| Local (Docker) | http://localhost:8080 |
| Local (Dev Mode) | http://localhost:8080 |

