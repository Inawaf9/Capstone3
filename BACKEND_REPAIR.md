# Sayyan backend repair

## Architecture

Eight JPA entities remain: User, Vehicle, MaintenanceRule, KilometerRecord,
MaintenanceRecord, Receipt, Notification, and AiChatHistory. Controllers retain existing
routes, services enforce business rules, repositories persist to MySQL. Active external
integrations are Vehicle Databases, OpenRouter, NHTSA VIN decoding, WhatsLoop, and SMTP.
OpenRouter is the sole AI provider. Receipt image extraction and the four vehicle AI
operations remain available.

Maintenance generation uses the restored `POST /api/v1/maintenance-rule/analyze/{vehicleId}`:
VIN → Maintenance API → bounded OpenRouter batches → source/mileage validation →
normalization/deduplication → append-only transactional persistence under a vehicle lock.
Fluids are supported by the restored client but never required or requested by generation.
Explicit kilometer values survive unchanged; only miles-only entries are converted.

## Removed and restored code

Removed all 15 retired manual-analysis Java files, the direct Google starter/properties,
PDFBox and Spring AI PDF reader dependencies, obsolete repository members, and the
manual-only asynchronous annotation. The entity was already missing; no replacement
manual entity or table was created. Also removed the unused empty Model/ReceiptService
class and ReceiptServiceDTO after reference checks.

Restored VehicleDatabaseClient, VehicleMaintenanceResponse, VehicleFluidsResponse,
MaintenanceAiRequest, MaintenanceAiResponse, MaintenanceAiRule, and MaintenancePrompts
from reviewed backup source. Integrated MaintenanceRuleService and its original endpoint
into the current code, retaining newer CRUD, cost, receipt, and notification functionality.
No archive `.git`, `.idea`, `target`, or generated content was restored.

## Original audit findings

IDs match the user's supplied original audit, not a newly invented numbering scheme.
FIXED means the source defect was repaired or its obsolete execution path removed;
it does not claim live production services or MySQL have been exercised. The latest
user instruction removes password hashing and all test infrastructure and restores the
original exception handling; those overrides are reflected below.

| ID | Status | Resolution / remaining work |
|---|---|---|
| 01 | FIXED | All actual POM/Java conflict blocks reconstructed; clean compilation checked. |
| 02 | FIXED | Maintenance record cost method closed; duplicate lookup removed. |
| 03 | FIXED | Invalid out-of-interface repository fragment removed. |
| 04 | FIXED | Missing-entity imports and incompatible worker/setter chain removed together. |
| 05 | FIXED | Obsolete maintenance-record relationship query removed. |
| 06 | FIXED | Notification user lookup uses the declared repository method. |
| 07 | FIXED | Duplicate methods and route shapes removed; active lookup returns structured JSON. |
| 08 | FIXED | Only OpenAI-compatible starter remains; explicit model selector; one intended ChatModel configuration inspected statically. |
| 09 | PARTIALLY FIXED | Positive identifiers, ownership association checks, safe server fields, and guarded destructive operations added. No authentication/principal-based authorization, as explicitly requested; endpoints remain unsuitable for public exposure. |
| 10 | PARTIALLY FIXED | Passwords excluded from JSON. Hashing removed at user request; new/changed values are stored as supplied. Existing database values are unchanged. |
| 11 | FIXED | Mileage add/record/update and vehicle updates share a monotonic policy; latest deletion blocked; historical edits/deletes defined. |
| 12 | FIXED | Related mileage writes transactional; pessimistic vehicle locks and refresh prevent concurrent stale odometer updates; concurrency protection retained in source. |
| 13 | FIXED | Shared schedule policy selects the latest completion per time-only rule and closes each completed absolute entry. |
| 14 | FIXED | First kilometer-based services checked without prior records. Missing time baseline explicitly UNKNOWN, not invented from registration date. |
| 15 | FIXED | Nullable mileage handled without unboxing; scheduler isolates each vehicle failure. |
| 16 | FIXED | RestClient replaces curl; HTTP errors and non-success provider envelopes fail delivery; bounded timeouts and safe JSON. |
| 17 | PARTIALLY FIXED | Durable generated-message keys, vehicle/notification locks, reuse of failed rows, throttle prevent normal duplicate creation/sending. A process crash after provider acceptance but before DB commit cannot provide exactly-once delivery without provider idempotency/reconciliation. |
| 18 | FIXED | User request DTO, read-only IDs/timestamps/children/status, ignored parent links, and fresh/sanitized create entities prevent ID/association mass assignment. |
| 19 | FIXED | AI receives deterministic states and latest service dates; absolute mileage is never treated as recurring. |
| 20 | FIXED | JPEG/PNG content decoding, size/pixel limits, structured receipt validation and parent recheck before persistence. |
| 21 | FIXED | 1–200 character questions validated before inference and aligned with history storage. |
| 22 | FIXED | System instructions separated from JSON data; bounded context and truncation flags. |
| 23 | FIXED | Removed blanket outer retries. SDK alone handles transient retries; configured one retry and 30-second request timeout; no parse/business-validation retry loop. |
| 24 | FIXED | Receipt prompt uses `services`; extraction names explicitly preview-only; existing receipt storage/relationships preserved. |
| 25 | PARTIALLY FIXED | Finite/nonnegative monetary validation added; report summation uses decimal arithmetic. Persisted monetary fields remain Double to preserve existing schema/API; full BigDecimal/DECIMAL conversion requires an approved data migration. |
| 26 | FIXED | Completed dates, finite costs, current-mileage ceiling, and chronology against readings/other services validated. |
| 27 | FIXED | Referenced rules cannot be deleted or have their schedule changed; cascading removal of completed history disabled. |
| 28 | NOT FIXED | Original exception handling restored at user request: handled exceptions return HTTP 400 and their messages. |
| 29 | FIXED | Report receipts/notifications filtered independently by their own timestamps, including reminders without completed records. |
| 30 | FIXED | Report readings sorted; known period baseline selected; insufficient readings reported instead of inventing distance. |
| 31 | FIXED | Retry dispatches by stored EMAIL/WHATSAPP channel; failure remains FAILED. |
| 32 | FIXED | Report outcome is explicit, no completed record required, previous calendar-month boundaries used. |
| 33 | FIXED | VIN syntax before HTTP, error code/returned VIN/model/year and entity validation before persistence; fixture wire casing explicit; client timeout configured. |
| 34 | FIXED | Retired remote PDF download/buffering pipeline removed. |
| 35 | FIXED | Retired destructive manual replacement removed; restored analysis rejects empty/partial output and preserves existing records. |
| 36 | FIXED | Obsolete asynchronous job/status mechanism removed. |
| 37 | FIXED | DB credentials externalized; default schema action is validate. All test configuration removed by request. |
| 38 | NOT FIXED | All test source, fixtures, mocks, test-only dependencies, and explicit test compiler configuration removed at user request. Verification is compilation/package plus static inspection only. |
| 39 | FIXED | Email template values HTML-escaped. |
| 40 | FIXED | Ignored Errors parameters removed; schedule requires mileage/month interval/condition; bean validation active. |
| 41 | FIXED | Redundant web starter and deprecated AI option properties removed; active Spring AI property names checked against cached metadata. |
| 42 | FIXED | Empty/unreferenced receipt artifacts removed; active Vehicle Databases source restored; mapper/AI dependencies now genuinely used. |
| 43 | FIXED | Clean Maven builds remove stale bytecode; no generated archive content restored. |
| 44 | FIXED | Template stream closed with try-with-resources; multiline report uses preformatted HTML. |
| 45 | FIXED | README, developer help and this report document setup, endpoints, semantics, validation and migration limits. |

Totals after the latest requested overrides: **39 FIXED; 4 PARTIALLY FIXED; 2 NOT FIXED; 0 NOT APPLICABLE**.
The residual authentication risk is substantial despite the PARTIALLY FIXED category.

## Refactoring and compatibility

- Constructor injection and consistent source formatting retained across the package structure.
- One MaintenanceScheduleService computes states for both reminders and AI.
- One odometer write method serves both mileage create endpoints and vehicle updates.
- Transactional locks use READ_COMMITTED and refresh after flushing pending writes, so
  waiting requests see newly committed state without discarding their own work.
- API error body remains `{ "message": "..." }`; the original exception handlers return HTTP 400.
- Request entities retain legacy shapes except UserRequest; server-owned fields cannot
  be written by JSON. Collection relationships are readable but no longer cascade deletion.
- No new authentication framework, entity, primary-key strategy, or database column added.
- Receipt AI creates return 201. Manual routes are intentionally gone. Reports return an
  explicit outcome. Test notifications now use normal tracking/throttling.

## Build Verification

Executed successfully with Java 25:

```sh
JAVA_HOME=/tmp/sayyan-jdk25/jdk-25.0.4.1+1/Contents/Home mvn clean package -Dmaven.test.skip=true
```

**BUILD SUCCESS.** Application compilation and Spring Boot JAR packaging passed.
Test compilation/execution were skipped; no test reports were generated.
The clean build removed previous generated test artifacts.

Static inspection covers controller route shapes, repository property/query paths,
constructor dependencies, configuration and the OpenRouter-only provider selection.
No actual conflict markers or obsolete manual/direct-Gemini source references remain.
`ApiException.java` and `ControllerAdvice.java` match their original HEAD versions.

No application startup, Spring context initialization, live MySQL, external API calls,
or real notification delivery was performed for this revision. These are runtime
limitations, not claims of successful verification.

## Deployment and migration risks

1. **Authentication remains absent.** All global reads, user/vehicle/history mutations,
   paid AI/upload endpoints, and messaging routes need authenticated ownership/roles
   before public deployment. A caller-supplied user ID is not proof of identity.
2. **Passwords:** hashing and its helper were removed by explicit request. New and
   changed passwords are stored as supplied. Existing rows were not changed; previously
   stored hashes, if any, remain unchanged. Password fields stay excluded from API JSON.
3. **Schema:** retained `maintenance_rule.maintenance_condition`, supported by backup code
   and the local saved data-source snapshot. Live MySQL was not inspected. If deployed data
   instead uses `rule_condition` or manual-linked rules, back up, inspect both columns/keys,
   copy/reconcile condition values, and backfill vehicle IDs while preserving rule IDs and
   all record/receipt references. Verify nulls, duplicates and foreign keys before switching.
   No renames/drops/data migrations executed. Default `ddl-auto=validate` fails safely on
   mismatch instead of silently creating a replacement column.
4. **Money:** full decimal persistence remains pending. Inventory column types and values;
   choose precision/scale and rounding, validate a shadow/backed-up conversion, then update
   Java DTOs/entities/queries and the schema together. No automatic conversion was attempted.
5. **Delivery:** locks prevent ordinary concurrent duplicates, but cannot atomically commit
   with an external provider. A crash or ambiguous timeout after acceptance can cause a retry
   duplicate. Provider idempotency keys/outbox/reconciliation would require a separate design.
   SENT represents provider acceptance, not confirmed handset delivery or recipient reading.
   WhatsLoop path, fields, bearer header and success envelope were checked against its
   [official quickstart](https://docs.whatsloop.net/docs/quickstart); live account behavior is untested.
6. **AI normalization:** source/mileage coverage and normalized-key deduplication are enforced,
   but semantic aliases produced differently on repeated runs may still require review.
   Data from models is not an authoritative maintenance schedule: only source mileage is accepted.
   Large schedules are bounded to 1,000 entries and processed synchronously in batches of ten;
   they can take multiple provider calls. Model/account availability and costs remain live checks.
7. **Time-only rules:** without a known completion date, reminders remain UNKNOWN. Neither a
   registration date nor invented service date is substituted for a maintenance baseline.
8. **Read scalability:** legacy unpaginated entity responses and Open Session in View remain
   for API compatibility; large histories can still amplify queries and response size.
9. **Toolchain:** use Java 25. Earlier installed Java 26/27 attempts failed inside Lombok.
   The Java 25 package build emits a Lombok Unsafe deprecation warning but succeeds.

## Latest cleanup changes

- Deleted `src/test/` entirely, including Java classes, fixtures, configuration and mocks.
- Deleted `Service/PasswordService.java`; removed its injection and calls from UserService.
- Removed H2 and the three Spring Boot test starters from pom.xml, along with the explicit
  test-compile annotation-processor execution.
- Restored `Api/ApiException.java` and `Advice/ControllerAdvice.java` from the original branch
  HEAD; converted all newer exception factory/status-constructor calls to the original constructor.
- Removed the scheduling condition introduced for the deleted test profile.
- Updated README.md, this report, and locally ignored HELP.md to describe build-only verification.

Other restored application functionality remains in place. No database changes, external
requests, commit, push, or branch switch was performed. The backup archive is unchanged.
