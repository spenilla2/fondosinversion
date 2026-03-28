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
| id (PK) | String | Identificador único |
| name | String | Nombre del cliente |
| email | String | Correo electrónico |
| phone | String | Teléfono |
| balance | Number | Saldo disponible (COP) |
| preferredNotification | String | EMAIL o SMS |
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

| Método | Endpoint | Descripción | Auth |
|---|---|---|---|
| POST | /api/auth/login | Autenticarse | No |
| GET | /api/funds | Listar fondos | No |
| POST | /api/funds/subscribe | Suscribirse a un fondo | JWT |
| POST | /api/funds/cancel | Cancelar suscripción | JWT |
| GET | /api/funds/transactions | Historial de transacciones | JWT |

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

## Cliente de Prueba

- Email: `demo@btgpactual.com`
- Password: `btg2025`
- Saldo inicial: COP $500.000

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
