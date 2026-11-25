# Tổng hợp: Giao tiếp, Kafka và Reliability trong hệ thống phân tán

## Mục lục

1. [Bối cảnh và mục tiêu](#1-bối-cảnh-và-mục-tiêu)
2. [Giao tiếp đồng bộ và bất đồng bộ](#2-giao-tiếp-đồng-bộ-và-bất-đồng-bộ)
   1. [Synchronous Communication](#21-synchronous-communication)
   2. [Asynchronous Communication và Event](#22-asynchronous-communication-và-event)
   3. [Coupling và temporal coupling](#23-coupling-và-temporal-coupling)
   4. [Khi nào dùng Messaging](#24-khi-nào-dùng-messaging)
   5. [So sánh và lưu ý về lỗi](#25-so-sánh-và-lưu-ý-về-lỗi)
3. [Kiến trúc Kafka](#3-kiến-trúc-kafka)
   1. [Luồng dữ liệu tổng quát](#31-luồng-dữ-liệu-tổng-quát)
   2. [Các thành phần](#32-các-thành-phần)
   3. [Partition, offset và thứ tự](#33-partition-offset-và-thứ-tự)
   4. [Consumer Group và khả năng mở rộng](#34-consumer-group-và-khả-năng-mở-rộng)
   5. [Replication và khả năng chịu lỗi](#35-replication-và-khả-năng-chịu-lỗi)
4. [Reliability và Delivery Semantics](#4-reliability-và-delivery-semantics)
   1. [At-most-once](#41-at-most-once)
   2. [At-least-once](#42-at-least-once)
   3. [Exactly-once](#43-exactly-once)
   4. [So sánh](#44-so-sánh)
5. [Idempotency và chống xử lý trùng](#5-idempotency-và-chống-xử-lý-trùng)
   1. [Idempotent Operation và Duplicate Message](#51-idempotent-operation-và-duplicate-message)
   2. [Idempotency Key và Processed Event Table](#52-idempotency-key-và-processed-event-table)
   3. [Unique Constraint và Database Transaction](#53-unique-constraint-và-database-transaction)
6. [Thực hành thiết kế một luồng Kafka đáng tin cậy](#6-thực-hành-thiết-kế-một-luồng-kafka-đáng-tin-cậy)
7. [Thuật ngữ chính](#7-thuật-ngữ-chính)
8. [Tài liệu nguồn](#8-tài-liệu-nguồn)

## 1. Bối cảnh và mục tiêu

Trong hệ thống phân tán, các ứng dụng thường trao đổi dữ liệu qua mạng. Mạng có thể chậm hoặc mất kết nối, một dịch vụ có thể dừng hoạt động, và message có thể được gửi lại sau khi xử lý lỗi. Vì vậy, thiết kế cần trả lời hai câu hỏi: các thành phần giao tiếp với nhau theo cách nào, và hệ thống giữ cho kết quả nghiệp vụ đúng ra sao khi có sự cố.

Tài liệu này nối ba chủ đề: lựa chọn giao tiếp đồng bộ hay bất đồng bộ; Kafka tổ chức và chuyển message như thế nào; và các cơ chế reliability như delivery semantics, idempotency, transaction và replication.

## 2. Giao tiếp đồng bộ và bất đồng bộ

### 2.1. Synchronous Communication

Trong giao tiếp đồng bộ, caller gửi yêu cầu và chờ receiver trả kết quả trước khi tiếp tục. REST API là ví dụ phổ biến: ứng dụng gọi dịch vụ đặt vé và chờ phản hồi xác nhận.

Ưu điểm là luồng xử lý dễ theo dõi, có phản hồi trực tiếp và thường đơn giản khi debug. Đổi lại, caller phụ thuộc vào khả năng phản hồi của receiver. Nếu receiver chậm hoặc không khả dụng, thời gian chờ và lỗi có thể lan ngược về caller.

### 2.2. Asynchronous Communication và Event

Trong giao tiếp bất đồng bộ, producer gửi message hoặc event qua một trung gian như Kafka rồi tiếp tục công việc mà không cần chờ consumer hoàn tất. Consumer đọc message và xử lý vào thời điểm phù hợp.

Event mô tả một sự việc đã xảy ra, chẳng hạn `PaymentSucceeded`. Nhiều consumer có thể quan tâm cùng một event: Booking Service xác nhận booking, còn Notification Service gửi thông báo. Producer không cần gọi từng consumer trực tiếp.

Giao tiếp bất đồng bộ làm giảm sự phụ thuộc trực tiếp giữa producer và consumer, đồng thời hỗ trợ xử lý nền, hấp thụ tải và mở rộng xử lý. Đổi lại, luồng xử lý trải qua nhiều thành phần hơn; việc theo dõi, xử lý lỗi và bảo đảm thứ tự thường phức tạp hơn.

### 2.3. Coupling và temporal coupling

Coupling là mức độ một thành phần phụ thuộc vào thành phần khác. Với lời gọi đồng bộ trực tiếp, caller cần biết endpoint và thường cần receiver sẵn sàng ngay lúc gọi. Đây là temporal coupling: hai phía phải cùng hoạt động trong một khoảng thời gian.

Broker giúp producer và consumer không cần hoạt động đồng thời ở cùng một thời điểm. Tuy nhiên, điều này không loại bỏ mọi phụ thuộc: hai phía vẫn cần thống nhất schema, ý nghĩa event, chính sách lưu giữ và cách xử lý lỗi.

### 2.4. Khi nào dùng Messaging

Messaging phù hợp khi cần chạy công việc nền; một event cần nhiều consumer; cần hấp thụ tải lớn; hoặc consumer có thể xử lý chậm hơn producer. Producer có thể đưa message vào broker, còn consumer xử lý dần theo năng lực.

Giao tiếp đồng bộ phù hợp khi caller cần kết quả ngay để quyết định bước tiếp theo. Có thể kết hợp cả hai: API đồng bộ tiếp nhận yêu cầu và trả mã tiếp nhận, sau đó phần xử lý dài được thực hiện bất đồng bộ.

### 2.5. So sánh và lưu ý về lỗi

| Tiêu chí | Đồng bộ | Bất đồng bộ |
|---|---|---|
| Caller chờ kết quả | Thường có | Thường không chờ consumer hoàn tất |
| Receiver phải sẵn sàng ngay | Thường cần | Không nhất thiết |
| Phản hồi trực tiếp | Có | Thường không; kết quả có thể đến qua event khác |
| Xử lý nền và tải lớn | Ít phù hợp hơn | Phù hợp |
| Một sự kiện cho nhiều nơi xử lý | Cần gọi hoặc điều phối riêng | Phù hợp với mô hình publish/subscribe |
| Theo dõi và xử lý lỗi | Thường trực tiếp hơn | Cần retry, quan sát luồng và xử lý message lỗi |

Async không đồng nghĩa với không có lỗi. Hệ thống vẫn cần chính sách retry cho lỗi tạm thời, xử lý message trùng, idempotency, Dead Letter Queue (DLQ) cho message không xử lý được, theo dõi consumer và thiết kế thứ tự khi nghiệp vụ yêu cầu.

## 3. Kiến trúc Kafka

### 3.1. Luồng dữ liệu tổng quát

Kafka là nền tảng phân tán để truyền và lưu trữ record. Producer ghi record vào topic; Kafka lưu record trong các partition; consumer đọc và xử lý chúng. Có thể thêm broker để phân phối lưu trữ và thêm consumer để tăng năng lực xử lý.

```text
Producer → Kafka Topic (các Partition trên Kafka Broker) → Consumer Group
```

### 3.2. Các thành phần

| Thành phần | Vai trò |
|---|---|
| Kafka Cluster | Tập hợp các broker phối hợp vận hành |
| Broker | Máy chủ Kafka nhận, lưu trữ và phục vụ record |
| Topic | Nhóm logic dùng để phân loại dữ liệu, ví dụ `payments` hoặc `notifications` |
| Partition | Phần của topic, là log record được lưu nối tiếp và đơn vị phân phối song song |
| Producer | Ứng dụng ghi record vào topic |
| Consumer | Ứng dụng đọc và xử lý record |
| Consumer Group | Nhóm consumer phối hợp chia partition để xử lý |
| Record/Event | Đơn vị dữ liệu được ghi vào Kafka |
| Offset | Vị trí của record trong một partition |
| Replica | Bản sao của partition trên broker khác |

Một record thường gồm key, value, timestamp và headers. Value có thể là JSON, String, Avro, Protobuf hoặc định dạng khác. Key có thể được dùng để chọn partition; các record cùng key thường được đưa vào cùng partition theo cơ chế phân vùng đã cấu hình.

### 3.3. Partition, offset và thứ tự

Mỗi partition hoạt động như một append-only log: record mới được nối vào cuối và nhận offset. Offset chỉ có ý nghĩa trong phạm vi partition đó.

Kafka bảo đảm thứ tự trong một partition, không bảo đảm thứ tự tổng thể giữa các partition. Nếu nghiệp vụ cần giữ thứ tự cho một thực thể, chẳng hạn các thay đổi trạng thái của cùng một booking, cần chọn key phù hợp để các event đó đi vào cùng partition.

### 3.4. Consumer Group và khả năng mở rộng

Trong một consumer group, mỗi partition được gán cho tối đa một consumer tại một thời điểm. Nhiều consumer có thể xử lý các partition khác nhau song song. Do đó, số consumer hoạt động song song trong group bị giới hạn bởi số partition đang được gán; consumer vượt quá số partition có thể không được giao partition.

Consumer group cũng là đơn vị theo dõi tiến độ đọc (offset đã commit). Các group khác nhau có thể đọc cùng một topic độc lập, ví dụ Booking Service và Notification Service cùng nhận `PaymentSucceeded`.

### 3.5. Replication và khả năng chịu lỗi

Kafka sao chép partition giữa các broker để giảm nguy cơ gián đoạn hoặc mất dữ liệu khi broker gặp sự cố. Replica leader xử lý đọc/ghi thông thường; follower sao chép dữ liệu từ leader. Khi leader gặp sự cố, một replica đủ điều kiện có thể được chọn làm leader mới.

Replication Factor cho biết mỗi partition có bao nhiêu replica. Ví dụ, topic có 3 partition và replication factor 3 thì có tổng cộng 9 bản sao partition. Tăng replication factor thường tăng khả năng chịu lỗi nhưng cũng tăng chi phí lưu trữ và đồng bộ.

Replication factor tự nó không đảm bảo mọi record đã được ghi bền vững. Độ bền còn phụ thuộc cấu hình producer như `acks`, cấu hình `min.insync.replicas`, trạng thái các replica và điều kiện bầu leader. Cần chọn các cấu hình này theo mức độ chấp nhận mất dữ liệu và tính sẵn sàng của hệ thống.

## 4. Reliability và Delivery Semantics

Reliability là khả năng hệ thống tiếp tục tạo ra kết quả đúng khi gặp lỗi như mất kết nối, ứng dụng dừng hoặc database không phản hồi. Delivery Semantics mô tả mức bảo đảm khi message được nhận và xử lý. Một điểm lỗi thường gặp là consumer đã xử lý nghiệp vụ nhưng chưa commit offset; khi chạy lại, Kafka có thể phát lại record.

### 4.1. At-most-once

At-most-once xử lý message tối đa một lần. Cách thường gặp là commit offset trước khi thực hiện nghiệp vụ. Nếu consumer dừng sau khi commit nhưng trước khi xử lý, message sẽ không được phát lại theo offset đã commit và nghiệp vụ có thể bị bỏ sót.

Cách này đơn giản và hạn chế phát lại do Kafka, phù hợp khi có thể chấp nhận thiếu một số dữ liệu như một vài sự kiện telemetry không trọng yếu. Không phù hợp với nghiệp vụ cần bảo đảm như thanh toán hoặc xác nhận đơn.

### 4.2. At-least-once

At-least-once xử lý nghiệp vụ trước rồi mới commit offset. Nếu consumer dừng sau khi nghiệp vụ thành công nhưng trước khi commit, record có thể được đọc lại. Cách này giảm nguy cơ bỏ sót message nhưng có thể gây xử lý trùng.

Ví dụ: lần đầu consumer tạo vé cho booking thành công nhưng chưa commit offset; sau khi khởi động lại, consumer nhận lại `PaymentSucceeded` và có thể tạo thêm vé. Vì vậy, at-least-once thường cần kết hợp idempotency.

### 4.3. Exactly-once

Exactly-once cần được hiểu trong một phạm vi cụ thể. Kafka hỗ trợ giao dịch để phối hợp ghi record đầu ra vào Kafka và commit offset đầu vào trong một transaction. Producer idempotent, transactional producer, transactional offset commit và consumer dùng `isolation.level=read_committed` là các cơ chế liên quan.

Nếu transaction thành công, output record và offset được commit cùng nhau; nếu thất bại, output chưa commit không được consumer `read_committed` nhìn thấy. Tuy nhiên, consumer hoặc processor vẫn có thể thực thi lại. Bảo đảm ở đây là kết quả đã commit trong phạm vi Kafka transaction không bị nhân đôi theo cách thông thường.

Kafka transaction không tự bao gồm PostgreSQL, email hay API thanh toán bên ngoài. Nếu một consumer cập nhật database rồi dừng trước khi commit Kafka offset, record có thể được phát lại và side effect ngoài Kafka có thể lặp. Những trường hợp này cần idempotency, transaction trong database, hoặc mô hình phối hợp phù hợp như outbox/inbox tùy kiến trúc.

### 4.4. So sánh

| Semantics | Nguy cơ bỏ sót xử lý | Nguy cơ xử lý lặp | Điểm cần nhớ |
|---|---|---|---|
| At-most-once | Có | Thấp do record đã commit không được phát lại theo offset | Ưu tiên không lặp, chấp nhận khả năng mất xử lý |
| At-least-once | Thấp hơn nhờ chỉ commit sau xử lý | Có | Cần consumer idempotent |
| Exactly-once | Giảm trong phạm vi transaction hỗ trợ | Thực thi lại vẫn có thể xảy ra; kết quả commit được bảo vệ trong phạm vi đó | Không mặc nhiên bao gồm side effect ngoài Kafka |

## 5. Idempotency và chống xử lý trùng

### 5.1. Idempotent Operation và Duplicate Message

Một thao tác idempotent cho kết quả nghiệp vụ tương đương dù cùng đầu vào được áp dụng nhiều lần. Ví dụ, đặt trạng thái booking thành `CONFIRMED` có thể idempotent nếu không kèm tác dụng phụ lặp. Ngược lại, mỗi lần gọi `createTicket()` tạo một vé mới thì thao tác đó không idempotent.

Duplicate Message có thể là cùng Kafka record được đọc lại sau lỗi, hoặc cùng một sự kiện nghiệp vụ được producer phát thành nhiều record khác nhau. Cả hai đều có thể khiến tác dụng phụ bị lặp.

### 5.2. Idempotency Key và Processed Event Table

Idempotency Key là mã ổn định để nhận diện yêu cầu/event đã xử lý. Có thể dùng `eventId`; khi publish lại cùng một event, phải giữ nguyên `eventId`. Không nên tạo ID mới cho mỗi lần retry nếu muốn nhận diện đó là cùng một thao tác nghiệp vụ.

Consumer có thể lưu dấu đã xử lý trong bảng `processed_events`, ví dụ gồm `consumer_name`, `event_id` và `processed_at`. Khóa duy nhất kết hợp `(consumer_name, event_id)` cho phép mỗi consumer ghi nhận riêng tiến độ của mình: cùng event có thể được Booking Service và Notification Service xử lý độc lập, nhưng mỗi service chỉ xử lý một lần.

Chỉ kiểm tra bảng rồi mới xử lý là chưa đủ: hai tiến trình có thể cùng kiểm tra thấy event chưa tồn tại và cùng tiếp tục. Đây là race condition. Cơ sở dữ liệu cần ràng buộc duy nhất để phân xử cạnh tranh một cách nguyên tử.

### 5.3. Unique Constraint và Database Transaction

Đặt unique constraint trên khóa idempotency trong `processed_events` giúp database từ chối lần ghi dấu trùng. Ngoài ra nên áp dụng constraint cho bất biến nghiệp vụ, ví dụ `UNIQUE(booking_id)` trên bảng tickets nếu mỗi booking chỉ được có một vé. Constraint là lớp bảo vệ cuối cùng ngay cả khi có xử lý đồng thời hoặc lỗi trong ứng dụng.

Một quy trình phổ biến cho consumer xử lý event trong cùng database:

1. Mở database transaction.
2. Thử insert `(consumer_name, event_id)` vào `processed_events` với unique constraint; dùng thao tác insert có xử lý conflict để biết event đã được nhận trước đó hay chưa.
3. Nếu khóa đã tồn tại, bỏ qua nghiệp vụ và kết thúc transaction.
4. Nếu insert mới thành công, cập nhật booking và tạo vé trong cùng transaction.
5. Commit database transaction; sau đó commit Kafka offset theo chính sách consumer.

Nếu cập nhật booking hoặc tạo vé thất bại, database transaction rollback cả dấu processed lẫn thay đổi nghiệp vụ để lần phát lại có thể thử lại. Nếu database commit xong nhưng Kafka offset chưa commit, record có thể phát lại; unique key khiến lần sau được nhận diện là đã xử lý. Transaction và constraint nghiệp vụ cùng giúp tránh trạng thái nửa chừng và tác dụng phụ trùng trong database.

## 6. Thực hành thiết kế một luồng Kafka đáng tin cậy

Với luồng booking nhận `PaymentSucceeded`, có thể thiết kế theo các bước sau:

1. Producer tạo event có `eventId` ổn định cùng `bookingId`, rồi publish vào topic phù hợp. Cấu hình độ bền producer theo yêu cầu dữ liệu.
2. Chọn key như `bookingId` nếu cần giữ thứ tự event của cùng booking trong một partition.
3. Consumer group đọc event; thực hiện retry có giới hạn cho lỗi tạm thời và đưa lỗi không thể phục hồi vào DLQ để điều tra.
4. Consumer dùng `(consumer_name, event_id)` làm khóa idempotency; lưu dấu, cập nhật booking và tạo vé trong một database transaction.
5. Dùng unique constraint ở tầng database cho bất biến như một vé trên mỗi booking.
6. Commit offset sau khi kết quả database đã commit. Nếu consumer lỗi giữa hai bước, Kafka có thể phát lại record; idempotency làm lần phát lại an toàn.
7. Theo dõi lag, lỗi xử lý, retry và DLQ để phát hiện consumer dừng hoặc không theo kịp producer.

Một lựa chọn phổ biến cho hệ thống nghiệp vụ là delivery at-least-once kết hợp consumer idempotent và database transaction. Cách này thừa nhận message có thể được gửi lại, đồng thời ngăn việc phát lại tạo thêm hiệu ứng nghiệp vụ trong database.

## 7. Thuật ngữ chính

| Thuật ngữ | Ý nghĩa ngắn |
|---|---|
| Event | Bản ghi mô tả một sự việc đã xảy ra |
| Message/Record | Đơn vị dữ liệu được gửi và lưu trong broker |
| Offset | Vị trí record trong một partition |
| Retry | Thử lại xử lý sau lỗi, thường áp dụng cho lỗi tạm thời |
| DLQ | Nơi lưu message không thể xử lý theo luồng thông thường |
| Idempotency | Khả năng xử lý lặp mà không làm thay đổi kết quả nghiệp vụ cuối |
| Unique Constraint | Ràng buộc database ngăn trùng một giá trị hoặc tổ hợp giá trị |
| Database Transaction | Nhóm thao tác database được commit hoặc rollback như một đơn vị |
| Replication Factor | Số replica của mỗi partition |

