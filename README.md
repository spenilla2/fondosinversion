# BTG Pactual - Fondos de Inversión API

API REST para gestión de fondos de inversión con arquitectura hexagonal.

## Arquitectura

```
src/main/java/com/btg/fondos/
├── domain/                          # Capa de dominio (núcleo)
│   ├── model/                       # Entidades: Client, Fund, Transaction
│   ├── port/
│   │   ├── in/                      # Puertos de entrada (casos de uso)
│   │   └── out/                     # Puertos de salida (repositorios, notificaciones)
│   └── exception/                   # Excepciones de negocio
├── application/                     # Capa de aplicación
│   ├── service/                     # Implementación de casos de uso
│   └── dto/                         # DTOs de request/response
└── infrastructure/                  # Capa de infraestructura
    ├── adapter/
    │   ├── in/rest/                 # Controllers REST
    │   └── out/
    │       ├── persistence/         # Adaptadores DynamoDB
    │       └── notification/        # Adaptador AWS SES/SNS
    ├── config/                      # Configuraciones (AWS, Beans, Swagger, DataSeeder)
    └── security/                    # JWT, filtros, Spring Security
```

## Tecnologías

- Java 17 + Spring Boot 3.4.5
- AWS DynamoDB (NoSQL)
- AWS SES (Email) / AWS SNS (SMS)
- JWT para autenticación
- Swagger/OpenAPI 3
- Spring Actuator
- CloudFormation para IaC
- Docker + ECS Fargate

## Modelo de Datos NoSQL (DynamoDB)

### Tabla: clients
| Atributo | Tipo | Descripción |
|---|---|---|
| id (PK) | String | UUID único |
| user | String | Identificador de usuario (ej: client-001) |
| name | String | Nombre del cliente |
| email | String | Correo electrónico |
| phone | String | Teléfono |
| balance | Number | Saldo disponible (COP) |
| preferredNotification | String | EMAIL, SMS o SNS |
| subscribedFundIds | StringSet | IDs de fondos suscritos |
| password | String | Contraseña encriptada (BCrypt) |
| role | String | ROLE_CLIENT o ROLE_ADMIN |

### Tabla: funds
| Atributo | Tipo | Descripción |
|---|---|---|
| id (PK) | String | Identificador único |
| name | String | Nombre del fondo |
| minimumAmount | Number | Monto mínimo de vinculación |
| category | String | FPV o FIC |

### Tabla: transactions
| Atributo | Tipo | Descripción |
|---|---|---|
| id (PK) | String | UUID único |
| clientId (GSI) | String | ID del cliente |
| fundId | String | ID del fondo |
| fundName | String | Nombre del fondo |
| type | String | SUBSCRIBE o CANCEL |
| amount | Number | Monto de la transacción |
| timestamp | String | Fecha ISO-8601 |

## Endpoints

### Autenticación
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | /api/auth/login | Autenticarse y obtener JWT | No |

### Fondos
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| GET | /api/funds | Listar fondos disponibles | No |
| POST | /api/funds/subscribe | Suscribirse a un fondo | JWT (ROLE_CLIENT) |
| POST | /api/funds/cancel | Cancelar suscripción | JWT (ROLE_CLIENT) |
| GET | /api/funds/transactions | Historial de transacciones | JWT (ROLE_CLIENT) |

### Clientes
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | /api/clients | Crear cliente | JWT (ROLE_ADMIN) |
| GET | /api/clients | Listar todos los clientes | JWT (ROLE_ADMIN) |
| GET | /api/clients/search?user= | Buscar cliente por usuario | JWT (ROLE_ADMIN) |
| GET | /api/clients/{id} | Obtener cliente por ID | JWT (ADMIN o propio) |
| PUT | /api/clients/{id} | Actualizar cliente | JWT (ADMIN o propio) |
| DELETE | /api/clients/{id} | Eliminar cliente | JWT (ROLE_ADMIN) |

### Monitoreo
| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| GET | /actuator/health | Health check | No |
| GET | /swagger-ui.html | Documentación API | No |

## Ejecución Local

### Prerrequisitos
- Java 17+
- DynamoDB Local (Docker):
```bash
docker run -d -p 8000:8000 amazon/dynamodb-local
```

### Ejecutar
```bash
./gradlew bootRun
```

### Swagger UI
http://localhost:8081/swagger-ui.html

### Actuator
http://localhost:8081/actuator/health

## Usuarios de Prueba

| Usuario | Email | Password | Rol |
|---|---|---|---|
| client-001 | demo@btgpactual.com | btg2025 | ROLE_CLIENT |
| admin-001 | admin@btgpactual.com | admin2025 | ROLE_ADMIN |

## Reglas de Negocio

- Saldo inicial de cada cliente: COP $500.000
- Cada fondo tiene un monto mínimo de vinculación
- Si no hay saldo suficiente: "No tiene saldo disponible para vincularse al fondo \<nombre\>"
- Al cancelar suscripción, el monto se devuelve al saldo del cliente
- ROLE_ADMIN no puede suscribirse ni cancelar fondos
- Notificación por EMAIL (SES) o SMS (SNS) según preferencia del cliente

## Seguridad

- **Autenticación**: JWT Bearer Token
- **Autorización**: Roles (ROLE_CLIENT, ROLE_ADMIN) con @PreAuthorize
- **Encriptación**: BCrypt para contraseñas
- **HTTPS**: Configurar en ALB con certificado ACM
- **DynamoDB SSE**: Encriptación en reposo habilitada

## Despliegue AWS (CloudFormation)

```bash
aws cloudformation deploy \
  --template-file cloudformation/template.yml \
  --stack-name btg-fondos-dev \
  --parameter-overrides \
    Environment=dev \
    JwtSecret=<your-secret-key-min-32-chars> \
    SenderEmail=<verified-ses-email> \
  --capabilities CAPABILITY_NAMED_IAM
```

### Recursos creados:
- 3 tablas DynamoDB (clients, funds, transactions)
- VPC con 2 subnets públicas
- ALB + Target Group
- ECS Cluster + Fargate Service
- ECR Repository
- IAM Roles con permisos mínimos
- CloudWatch Log Group

## Tests

```bash
./gradlew test
```
