# 🛒 E-Commerce de Útiles Escolares - UADE

Proyecto desarrollado para la materia **Aplicaciones Interactivas** de la **Universidad Argentina de la Empresa (UADE)**.

El sistema consiste en una plataforma de **e-commerce de útiles escolares**, orientada a estudiantes de la universidad. Permite consultar productos, clasificarlos por categorías, gestionar usuarios, realizar pedidos y dejar reseñas sobre los productos adquiridos.

---

## 📚 Información del proyecto

- **Universidad:** Universidad Argentina de la Empresa (UADE)
- **Materia:** Aplicaciones Interactivas
- **Trabajo Práctico:** E-Commerce UADE
- **Comisión:** 16318
- **Profesor/a:** Sanchez Santos, Baltazar

### 👥 Integrantes

| Legajo | Apellido y nombre |
|---|---|
| 1225238 | Moyano, Benjamin |
| 1202784 | Parodi, Franco |
| 1164072 | Martin Garcia Berro |
| 1162678 | Kapusta, Nahuel |
| 1141525 | Marchioli, Orlando Nicolas |
| 1208650 | Romero, Tomás Augusto |
| 1144815 | Stella, Pedro |

---

## 📝 Descripción

El proyecto consiste en el desarrollo de un sistema de comercio electrónico para la venta de útiles escolares y productos relacionados con la vida universitaria.

La aplicación permite a los usuarios:

- Registrarse e iniciar sesión.
- Consultar el catálogo de productos.
- Visualizar información detallada de cada producto.
- Filtrar productos por categoría.
- Realizar pedidos.
- Consultar información de sus pedidos.
- Dejar reseñas y calificaciones sobre los productos.
- Gestionar productos y categorías según el rol del usuario.

El sistema contempla distintos tipos de usuarios y busca representar el funcionamiento básico de una plataforma de e-commerce.

---

## 🛠️ Tecnologías utilizadas

- **Frontend:** TBD
- **Backend:** Spring Boot, Spring Data JPA, Lombok y Maven.
- **Base de datos:** H2
- **Lenguaje/s:** Java
- **ORM / Librería de acceso a datos:** TBD
- **Control de versiones:** Git / GitHub

---

## 🗂️ Modelo de datos

El sistema utiliza una base de datos relacional compuesta por las siguientes entidades principales:

### Producto

Representa los útiles escolares disponibles en el catálogo.

Algunos de sus datos son:

- Código / identificador
- Nombre
- Descripción
- Precio
- Stock
- Tipo
- Categoría
- Publicador

### Categoría

Permite clasificar los productos dentro del catálogo.

Contiene información como:

- Identificador
- Nombre
- Descripción

### Usuario

Representa a los usuarios registrados en la plataforma.

Contiene:

- Identificador
- Nombre de usuario
- Email
- Contraseña
- Nombre
- Apellido
- Rol

### Pedido

Representa una compra realizada por un usuario.

Contiene:

- Identificador
- Fecha
- Estado
- Usuario comprador

### Item de pedido

Representa cada producto incluido dentro de un pedido.

Contiene:

- Identificador
- Cantidad
- Precio unitario
- Subtotal
- Producto
- Pedido

### Reseña

Permite que los usuarios califiquen y comenten productos.

Contiene:

- Identificador
- Producto
- Usuario
- Calificación
- Comentario
- Fecha

### Imagen de producto

Permite asociar una o más imágenes a cada producto.

Contiene:

- Identificador
- URL
- Orden de visualización
- Producto

---

## 🔗 Relaciones principales

Las principales relaciones del modelo son:

- Un **producto** pertenece a una **categoría**.
- Una **categoría** puede contener múltiples productos.
- Un **usuario** puede realizar múltiples pedidos.
- Un **pedido** pertenece a un usuario.
- Un **pedido** puede contener múltiples ítems.
- Cada **ítem de pedido** corresponde a un producto.
- Un **producto** puede tener múltiples imágenes.
- Un **producto** puede recibir múltiples reseñas.
- Un **usuario** puede escribir múltiples reseñas.
- Cada reseña pertenece a un producto y a un usuario.

---

## 🏗️ Arquitectura

La aplicación está organizada utilizando una arquitectura basada en:

```text
Frontend
   │
   ▼
Backend / API
   │
   ▼
Base de Datos
```
