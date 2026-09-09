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

### Carrito

Representa la compra en curso de un usuario, antes de confirmarla. Cada usuario tiene un único carrito, que se crea la primera vez que lo consulta.

Contiene:

- Identificador
- Usuario

### Ítem de carrito

Representa cada producto agregado al carrito, con su cantidad.

Contiene:

- Identificador
- Cantidad
- Producto
- Carrito

El carrito no guarda precios: los toma del catálogo cada vez que se consulta, así siempre muestra el valor vigente. El total tampoco se almacena, se calcula en la respuesta.

### Pedido

Representa una compra realizada por un usuario. Se genera a partir del contenido del carrito y, desde ese momento, deja de depender de él.

Contiene:

- Identificador
- Fecha
- Estado
- Total
- Usuario comprador
- Ítems

### Item de pedido

Representa cada producto incluido dentro de un pedido.

Contiene:

- Identificador
- Cantidad
- Precio unitario (congelado al momento de la compra)
- Nombre del producto (copiado al momento de la compra)
- Producto
- Pedido

A diferencia del ítem de carrito, que toma el precio actual del catálogo, el ítem de pedido guarda su propia copia del precio y del nombre. Así, si un producto cambia de precio o se renombra, los pedidos anteriores siguen reflejando lo que el usuario efectivamente compró. El subtotal no se almacena: al ser cantidad × precio unitario, se calcula en la respuesta para que no pueda quedar desincronizado.

### Importes

Todos los valores monetarios del sistema usan `BigDecimal` y se guardan como `DECIMAL(12,2)`.

El motivo es que `double` no puede representar de forma exacta la mayoría de los importes con centavos, y el error se vuelve visible apenas se multiplica: tres unidades de un producto de $8.35 daban $25.049999999999997 en lugar de $25.05. Con `BigDecimal` el cálculo es exacto.

Dos detalles a tener en cuenta al trabajar con este tipo:

- Las comparaciones se hacen con `compareTo` y no con `equals`, porque `equals` también compara la escala y considera que `800.0` es distinto de `800.00`. En los tests se usa `isEqualByComparingTo` por la misma razón.
- Las sumas se hacen con `add` y `multiply`; convertir a `double` en el medio para hacer una cuenta reintroduce exactamente el error que este tipo evita.

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
- Un **usuario** tiene un único **carrito**.
- Un **carrito** puede contener múltiples ítems, uno por producto.
- Un **usuario** puede realizar múltiples pedidos.
- Un **pedido** pertenece a un usuario.
- Un **pedido** puede contener múltiples ítems.
- Cada **ítem de pedido** corresponde a un producto.
- Un **producto** puede tener múltiples imágenes.
- Un **producto** puede recibir múltiples reseñas.
- Un **usuario** puede escribir múltiples reseñas.
- Cada reseña pertenece a un producto y a un usuario.

---

## 🧾 Del carrito al pedido (checkout)

El checkout es la operación que convierte un carrito en un pedido. Se ejecuta como una única transacción: si algún paso falla, no se guarda nada.

Al confirmar la compra, el sistema:

1. Bloquea el carrito y verifica que tenga al menos un ítem.
2. Bloquea los productos involucrados y vuelve a leer su stock.
3. Valida el stock de **todos** los ítems antes de modificar ninguno, para que un producto sin stock al final del carrito no deje a los anteriores ya descontados.
4. Crea el pedido en estado `PENDING`, copiando en cada ítem el nombre y el precio del producto.
5. Descuenta el stock de los productos físicos.
6. Calcula el total y guarda el pedido junto con sus ítems.
7. Vacía el carrito, para que la misma compra no pueda generarse dos veces.

El stock se valida nuevamente en este punto, aunque el carrito ya lo haya hecho al agregar el ítem: entre ambos momentos puede haber pasado tiempo y otro usuario puede haberse llevado esas unidades.

Los bloqueos de los pasos 1 y 2 resuelven problemas distintos, ambos de concurrencia.

El del **carrito** evita que una misma compra se confirme dos veces. Si el usuario hace doble clic en "comprar", las dos peticiones llegan casi a la vez y ambas encontrarían el carrito lleno, generando dos pedidos por la misma mercadería. Con el bloqueo, la segunda espera a que la primera termine y para entonces el carrito ya está vacío.

El de los **productos** protege el stock. Descontarlo implica leerlo, restarle una cantidad y volver a guardarlo; si dos compras hacen eso a la vez, ambas leen el mismo valor inicial y una termina pisando el descuento de la otra. Además de tomar el bloqueo, el stock se vuelve a leer desde la base de datos: el producto ya está cargado en memoria desde el carrito, y sin ese refresco se validaría contra un valor viejo, que es justamente lo que el bloqueo busca evitar. Los bloqueos se toman siempre ordenados por identificador de producto, para que dos compras simultáneas no queden esperándose mutuamente.

Los productos de tipo `SERVICE` (clases, cursos) no manejan stock, por lo que no se validan ni se descuentan.

### Estados del pedido

```text
PENDING ──▶ PAID ──▶ SHIPPED ──▶ DELIVERED
   │         │
   └────┬────┘
        ▼
    CANCELLED
```

- Un pedido nace en `PENDING` y solo puede avanzar a lo largo del flujo.
- `DELIVERED` y `CANCELLED` son estados finales: no admiten cambios posteriores.
- Un pedido puede cancelarse mientras no haya sido despachado. Al cancelarse, el stock reservado vuelve al catálogo.
- Una vez en `SHIPPED`, la mercadería ya salió y la cancelación deja de ser posible.

Las transiciones válidas están definidas en el propio enum `OrderStatus`, de modo que exista un único lugar donde consultarlas o modificarlas.

### Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/orders?userId={id}` | Genera el pedido a partir del carrito del usuario. No lleva body |
| `GET` | `/api/orders?userId={id}` | Historial de pedidos del usuario, del más reciente al más antiguo |
| `GET` | `/api/orders/{id}?userId={id}` | Detalle de un pedido con sus ítems |
| `PUT` | `/api/orders/{id}/status?userId={id}` | Cambia el estado del pedido. Body: `{ "status": "PAID" }` |

Todos los endpoints reciben el `userId`, y un pedido solo puede ser consultado o modificado por el usuario que lo realizó: conocer el identificador no alcanza para acceder a un pedido ajeno. Mientras el proyecto no tenga autenticación, ese dato viaja como parámetro; cuando la tenga, saldrá de la sesión y la validación se mantiene igual.

La cancelación no tiene un endpoint propio: es un cambio de estado más y se realiza mediante `PUT /api/orders/{id}/status` con `CANCELLED`, para que las reglas de transición se apliquen en un solo lugar.

### Respuestas de error

| Código | Situación |
|---|---|
| `400` | Carrito vacío, estado inexistente o transición no permitida |
| `403` | El pedido no pertenece al usuario que realiza la consulta |
| `404` | Pedido o usuario inexistente |
| `409` | Stock insuficiente al confirmar la compra, o intento de eliminar un producto que ya fue vendido |

Un pedido no se elimina: es un registro histórico de una operación. Por eso no existe un `DELETE` de pedidos, y un producto que ya fue vendido no puede borrarse, ya que sus ítems de pedido lo referencian.

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
