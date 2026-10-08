[README (1).md](https://github.com/user-attachments/files/33187892/README.1.md)
# 🚗 Vehicle Maintenance API
### واجهة برمجية لإدارة صيانة المركبات

**[English](#-english)** · **[العربية](#-العربية)**

---

## 🇬🇧 English

### 📌 Overview

This project is larger than what is documented here. This README covers **only my part of the work: the Maintenance module**, made of two resources, **Maintenance Rule** and **Maintenance Record**. Each has **8 basic CRUD operations**, plus **12 additional functions** (including both lookups by ID) described below.

| | |
|---|---|
| **Base paths** | `/api/v1/maintenanceRecord` and `/api/v1/maintenanceRule` |
| **Location** | `com.nawaf.capstone3.Controller` and `com.nawaf.capstone3.Service` |

### 🛠️ Additional Endpoints

| # | Endpoint | Problem addressed | Brief solution | Project location |
|---|---|---|---|---|
| 1 | `GET /api/v1/maintenanceRule/get/{maintenanceRuleId}` | Fetch one maintenance rule by its ID. | Find the rule by ID; throw an exception if it does not exist. | `MaintenanceRuleService`<br>`getMaintenanceRuleById` |
| 2 | `GET /api/v1/maintenanceRecord/get/{maintenanceRecordId}` | Fetch one maintenance record by its ID. | Find the record by ID; throw an exception if it does not exist. | `MaintenanceRecordService`<br>`getMaintenanceRecordById` |
| 3 | `GET /get-by-vehicleId/{vehicleId}` | Find the maintenance rules assigned to a vehicle. | Query rules using the vehicle ID. | `Service`<br>`getRuleByVehicleId` |
| 4 | `GET /get-Due-Maintenances/{maintenanceRecordId}` | Identify maintenance within the due window. | Calculate remaining km; due when between -1000 and 1000 km. | `Service`<br>`getDueMaintenances` |
| 5 | `GET /get-Over-due-Maintenances/{maintenanceRecordId}` | Identify maintenance beyond the allowed delay. | Return `OVERDUE` when remaining km is below -1000. | `Service`<br>`getOverdueMaintenances` |
| 6 | `GET /get-Upcoming-Maintenance/{maintenanceRecordId}` | Show the expected odometer reading for future maintenance. | Return `UPCOMING` when remaining km is greater than 1000. | `Service`<br>`getUpcomingMaintenance` |
| 7 | `POST /complet-Maintenance/{vehicleId}/{maintenanceRuleId}` | Document completed maintenance and update the odometer. | Check vehicle and rule ownership; save a record for today and increase the odometer if needed. | `Service`<br>`completeMaintenance` |
| 8 | `GET /get-Vehicle-Maintenance-History/{vehicleId}` | Review a vehicle's maintenance history in one response. | Return vehicle details, record count and records ordered by service date descending. | `Service`<br>`getVehicleMaintenanceHistory` |
| 9 | `PUT /update-note/{maintenanceRecordId}` | Change a record's note without updating all fields. | Read note from `UpdateNoteDTO`; find the record, set note and save. | `Service`<br>`updateNoteByRecordId` |
| 10 | `GET /get-records-by-service-name/{serviceName}` | Find records for a specific maintenance service. | Search by the related rule's service name, ignoring letter case. | `Service`<br>`getRecordsByServiceName` |
| 11 | `GET /get-records-by-vehicleId-and-workshop/{vehicleId}/{workshop}` | Find a vehicle's maintenance at a specified workshop. | Filter by vehicle ID and workshop name, ignoring letter case. | `Service`<br>`getRecordsByVehicleIdAndWorkshop` |
| 12 | `PUT /update-cost/{maintenanceRecordId}` | Change only the maintenance cost. | Receive cost separately; reject null or negative values, then update and save. | `Service`<br>`updateCostByRecordId` |

### 📐 Maintenance Status Formula

```text
Remaining km = record kilometers + rule kilometers - current vehicle kilometers
```

The three status functions evaluate the supplied record:

| Status | Condition (remaining km) | Meaning |
|---|---|---|
| 🔴 `OVERDUE` | `< -1000` | Beyond the allowed delay |
| 🟡 Due | `-1000 … 1000` | Within the allowed window |
| 🟢 `UPCOMING` | `> 1000` | Expected future maintenance |

### 🧰 Technologies

| Technology | Purpose |
|---|---|
| ☕ **Java** | Application language |
| 🌱 **Spring Boot** | Backend application framework |
| 🌐 **Spring Web MVC** | REST controllers and HTTP responses |
| 🗄️ **Spring Data JPA** | Repository interfaces and database queries |
| 🧬 **Hibernate & Jakarta Persistence** | Entity mapping and relationships |
| ✅ **Jakarta Bean Validation** | Request and field validation |
| 🔄 **Jackson** | JSON serialization and deserialization |
| ✂️ **Lombok** | Generate getters, setters and constructors |
| 🐬 **MySQL-compatible database** | Relational storage for vehicles, rules and records |
| 💡 **IntelliJ IDEA** | Development environment |
| 🔍 **DataGrip** | Database inspection and SQL execution |
| ✨ **Gemini AI** | AI used in the project |

---

<div dir="rtl">

## 🇸🇦 العربية

### 📌 نظرة عامة

المشروع أكبر مما يوثّقه هذا الملف. يغطي هذا الـ README **الجزء الذي عملتُ عليه فقط: وحدة الصيانة (Maintenance)**، وتتكوّن من موردين هما **قاعدة الصيانة** و**سجل الصيانة**. لكل منهما **8 عمليات CRUD أساسية**، إضافة إلى **12 دالة إضافية** (تشمل دالتي الجلب بالمعرّف) موضحة أدناه.

| | |
|---|---|
| **المسارات الأساسية** | `/api/v1/maintenanceRecord` و `/api/v1/maintenanceRule` |
| **الموقع** | `com.nawaf.capstone3.Controller` و `com.nawaf.capstone3.Service` |

### 🛠️ الدوال الإضافية للصيانة

| # | نقطة النهاية | المشكلة التي تحلها | طريقة الحل المختصرة | المكان داخل المشروع |
|---|---|---|---|---|
| 1 | `GET /api/v1/maintenanceRule/get/{maintenanceRuleId}` | جلب قاعدة صيانة محددة بمعرفها. | البحث بمعرف القاعدة وإظهار رسالة خطأ إذا لم توجد. | `MaintenanceRuleService`<br>`getMaintenanceRuleById` |
| 2 | `GET /api/v1/maintenanceRecord/get/{maintenanceRecordId}` | جلب سجل صيانة محدد بمعرفه. | البحث بمعرف السجل وإظهار رسالة خطأ إذا لم يوجد. | `MaintenanceRecordService`<br>`getMaintenanceRecordById` |
| 3 | `GET /get-by-vehicleId/{vehicleId}` | جلب قواعد الصيانة الخاصة بالمركبة. | البحث عن القواعد بمعرف المركبة. | `Service`<br>`getRuleByVehicleId` |
| 4 | `GET /get-Due-Maintenances/{maintenanceRecordId}` | تحديد الصيانة ضمن نطاق الاستحقاق. | حساب المتبقي؛ مستحقة من سالب 1000 إلى موجب 1000 كم. | `Service`<br>`getDueMaintenances` |
| 5 | `GET /get-Over-due-Maintenances/{maintenanceRecordId}` | تحديد الصيانة التي تجاوزت حد التأخير. | إرجاع `OVERDUE` عندما يكون المتبقي أقل من سالب 1000 كم. | `Service`<br>`getOverdueMaintenances` |
| 6 | `GET /get-Upcoming-Maintenance/{maintenanceRecordId}` | معرفة قراءة العداد المتوقعة للصيانة القادمة. | إرجاع `UPCOMING` عندما يكون المتبقي أكبر من 1000 كم. | `Service`<br>`getUpcomingMaintenance` |
| 7 | `POST /complet-Maintenance/{vehicleId}/{maintenanceRuleId}` | توثيق الصيانة المنجزة وتحديث عداد المركبة. | التحقق من المركبة وتبعية القاعدة؛ حفظ سجل بتاريخ اليوم وتحديث العداد إذا زادت القراءة. | `Service`<br>`completeMaintenance` |
| 8 | `GET /get-Vehicle-Maintenance-History/{vehicleId}` | عرض تاريخ صيانة المركبة في استجابة واحدة. | إرجاع بيانات المركبة وعدد السجلات والسجلات مرتبة حسب تاريخ الصيانة تنازليًا. | `Service`<br>`getVehicleMaintenanceHistory` |
| 9 | `PUT /update-note/{maintenanceRecordId}` | تعديل ملاحظة السجل دون تعديل بقية الحقول. | استقبال الملاحظة عبر `UpdateNoteDTO` ثم جلب السجل وتعديل الملاحظة وحفظه. | `Service`<br>`updateNoteByRecordId` |
| 10 | `GET /get-records-by-service-name/{serviceName}` | جلب سجلات خدمة صيانة محددة. | البحث باسم الخدمة في القاعدة المرتبطة دون حساسية لحالة الأحرف. | `Service`<br>`getRecordsByServiceName` |
| 11 | `GET /get-records-by-vehicleId-and-workshop/{vehicleId}/{workshop}` | جلب صيانة مركبة محددة في ورشة معينة. | التصفية بمعرف المركبة واسم الورشة دون حساسية لحالة الأحرف. | `Service`<br>`getRecordsByVehicleIdAndWorkshop` |
| 12 | `PUT /update-cost/{maintenanceRecordId}` | تعديل تكلفة الصيانة فقط. | استقبال التكلفة والتحقق من أنها ليست فارغة أو سالبة ثم تعديل السجل وحفظه. | `Service`<br>`updateCostByRecordId` |

### 📐 معادلة حالة الصيانة

```text
المتبقي = كيلومترات السجل + فترة القاعدة بالكيلومترات − عداد المركبة الحالي
```

ميثودات الحالة الثلاث تقيّم السجل المُدخل:

| الحالة | الشرط (المتبقي بالكم) | المعنى |
|---|---|---|
| 🔴 `OVERDUE` | `< -1000` | تجاوزت حد التأخير |
| 🟡 مستحقة | `-1000 … 1000` | ضمن النطاق المسموح به |
| 🟢 `UPCOMING` | `> 1000` | صيانة متوقعة قادمة |

### 🧰 التقنيات المستخدمة في المشروع

| التقنية | الغرض |
|---|---|
| ☕ **Java** | لغة برمجة المشروع |
| 🌱 **Spring Boot** | إطار تشغيل تطبيق الباك إند |
| 🌐 **Spring Web MVC** | إنشاء نقاط REST واستجابات HTTP |
| 🗄️ **Spring Data JPA** | واجهات الريبو واستعلامات قاعدة البيانات |
| 🧬 **Hibernate و Jakarta Persistence** | ربط المودلز بالجداول والعلاقات |
| ✅ **Jakarta Bean Validation** | التحقق من بيانات الطلبات والحقول |
| 🔄 **Jackson** | تحويل بيانات JSON والتحكم في ظهور الحقول |
| ✂️ **Lombok** | توليد دوال الوصول والبناء |
| 🐬 **قاعدة بيانات متوافقة مع MySQL** | تخزين المركبات والقواعد والسجلات في قاعدة علائقية |
| 💡 **IntelliJ IDEA** | بيئة تطوير المشروع |
| 🔍 **DataGrip** | فحص قاعدة البيانات وتنفيذ SQL |
| ✨ **Gemini AI** | استخدام الذكاء الاصطناعي Gemini في المشروع |

</div>
