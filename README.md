# SalusPet 🐾

> Una plataforma multiplataforma integral para centralizar el historial clínico y el cuidado de mascotas, eliminando la dependencia de la cartilla de papel física.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![.NET Core](https://img.shields.io/badge/.NET_Core-512BD4?style=for-the-badge&logo=dotnet&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)

## 📖 El Problema que resolvemos
Actualmente, la información médica de las mascotas está fragmentada: un papel en casa, una ficha en una clínica y otra diferente si tienes que acudir a urgencias. **SalusPet** unifica toda esta información en la nube para que el dueño siempre tenga el control y el historial de su mascota a mano, en cualquier dispositivo.

## 🚀 Funcionalidades "Estrella" (MVP)
- 📲 **Perfil Digital Único:** Creación de un perfil digital para la mascota con un código QR que contiene sus datos vitales y de contacto.
- 📅 **Calendario Inteligente:** Sincronización automática y recordatorios de vacunas, desparasitaciones y citas veterinarias.
- 📄 **Gestión Documental y Exportación PDF:** Capacidad de subir fotos de recetas/cartillas desde el móvil y generar un dossier clínico profesional con un clic desde la app de escritorio.
- 🗺️ **Geolocalización:** Mapa integrado para localizar clínicas y urgencias veterinarias cercanas.

## 🛠️ Arquitectura y Stack Tecnológico
SalusPet no es solo una aplicación, es un ecosistema completo compuesto por tres pilares fundamentales:

### 1. Backend (.NET Core + MySQL) 🧠
Es el "cerebro" del proyecto. Una API REST escalable y profesional encargada de gestionar la lógica de negocio, los usuarios, las mascotas y la seguridad de los datos en la nube.

### 2. App Móvil (Android con Kotlin + Jetpack Compose) 📱
La cara visible para el usuario masivo, pensada para el día a día.
- **Funciones:** Registro rápido de vacunas, captura de fotos de recetas médicas mediante la cámara del dispositivo, notificaciones push para recordatorios y geolocalización por GPS.

### 3. App de Escritorio (Java con IntelliJ) 💻
La herramienta de gestión avanzada, pensada para protectoras, clínicas o usuarios que requieren un control más exhaustivo.
- **Funciones:** Generación de informes médicos detallados en formato PDF, visualización de gráficas de salud/peso y gestión de bases de datos de forma más densa y cómoda.

## 🌟 ¿Por qué SalusPet destaca? (Visión del Proyecto)
- **Multiplataforma Real:** Cada dispositivo tiene su utilidad específica. El móvil para la inmediatez y el día a día; el escritorio para la gestión documental e informes.
- **Integración de Hardware:** Uso activo de características nativas del dispositivo (Cámara, GPS, Notificaciones Push).
- **Diferenciación de Mercado:** A diferencia de la competencia (como *11pets*), SalusPet ofrece una herramienta de escritorio vinculada, permitiendo un manejo de datos complejos y exportación a nivel profesional.
- **Potencial de Mercado:** Un producto preparado para producción que responde a una necesidad real de millones de dueños de mascotas.

---
*Proyecto desarrollado como Trabajo de Fin de Grado (TFG).*
