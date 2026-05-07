# SalusPet 🐾

> Una plataforma multiplataforma integral para centralizar el historial clínico y el cuidado de mascotas, eliminando la dependencia de la cartilla de papel física.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![NestJS](https://img.shields.io/badge/nestjs-%23E0234E.svg?style=for-the-badge&logo=nestjs&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)

## 📖 El Problema que resolvemos
Actualmente, la información médica de las mascotas está fragmentada: un papel en casa, una ficha en una clínica y otra diferente si tienes que acudir a urgencias. **SalusPet** unifica toda esta información en la nube para que el dueño siempre tenga el control y el historial de su mascota a mano, en cualquier dispositivo.

## 🚀 Funcionalidades "Estrella" (MVP)
- 📲 **Perfil Digital Único:** Creación de un perfil digital para cada mascota, permitiendo gestionar sus datos vitales y fotografía identificativa de forma centralizada.
- 📅 **Calendario Inteligente y Citas:** Sistema interactivo de solicitud de citas. El usuario selecciona mascota, clínica y profesional directamente desde la base de datos central.
- 📄 **Gestión Documental y Exportación PDF:** Acceso detallado a las consultas pasadas con la capacidad de descargar un dossier clínico profesional en PDF desde la app.
- 🔒 **Seguridad Anti-Bots (CAPTCHA):** Sistema de registro protegido con validación dinámica integrada en la interfaz para evitar la creación de cuentas masivas.

## 🛠️ Arquitectura y Stack Tecnológico
SalusPet no es solo una aplicación, es un ecosistema completo compuesto por tres pilares fundamentales:

### 1. Backend (NestJS + MySQL) 🧠
Es el "cerebro" del proyecto. Una API REST escalable, modular y profesional encargada de gestionar la lógica de negocio, los usuarios, las mascotas y la seguridad de los datos en la nube.

### 2. App Móvil (Android con Kotlin + Jetpack Compose) 📱
La cara visible para el usuario masivo, pensada para el día a día.
- **Funciones:** Gestión interactiva de citas médicas, consulta de historiales clínicos, registro seguro de usuarios y conexión en tiempo real con el servidor mediante Retrofit.

### 3. App de Escritorio (Java con IntelliJ) 💻
La herramienta de gestión avanzada, pensada para protectoras, clínicas o usuarios que requieren un control más exhaustivo.
- **Funciones:** Generación de informes médicos detallados en formato PDF (iTextPDF), interfaz gráfica modernizada (FlatLaf) y gestión de bases de datos de forma más densa y cómoda.

## 🌟 ¿Por qué SalusPet destaca? (Visión del Proyecto)
- **Multiplataforma Real:** Cada dispositivo tiene su utilidad específica. El móvil para la inmediatez y el día a día; el escritorio para la gestión documental e informes.
- **Arquitectura y Seguridad:** Uso de validaciones nativas, generación de documentos en el propio dispositivo y conexión segura mediante módulos independientes.
- **Diferenciación de Mercado:** A diferencia de la competencia (como *11pets*), SalusPet ofrece una herramienta de escritorio vinculada, permitiendo un manejo de datos complejos y exportación a nivel profesional.
- **Potencial de Mercado:** Un producto preparado para producción que responde a una necesidad real de millones de dueños de mascotas.

---
*Proyecto desarrollado como Trabajo de Fin de Grado (TFG).*
