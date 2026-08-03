# Phase 14 - Cache, Performance và Concurrency

## Cache invalidation

- Project write: detail, search/list và dashboard.
- Project member write: member list, dashboard, task/backlog/sprint theo quyền truy cập.
- Sprint write: current sprint, list/detail, Kanban, statistics, burndown và dashboard.
- Backlog write: backlog detail/list, Kanban, sprint statistics và dashboard.
- Task write: task detail/search, Kanban, statistics, burndown, project/my dashboard.
- Comment write: comment list, task detail và search liên quan.
- Time Log write: log list, summary, statistics, burndown, report và dashboard.
- Notification write: notification list, unread count và my dashboard.

Các thao tác trên dùng tập trung qua `CacheEvictService`; không để write service tự ghép cache key rải rác.

## N+1

- Task response và Kanban dùng batch map cho assignee, backlog item và tổng thời gian.
- Comment, Time Log, Notification và Activity dùng batch lookup author/actor.
- Dashboard và report ưu tiên aggregate query/projection thay vì truy vấn trong vòng lặp.

## Batch import

- Task import validate toàn bộ trước khi ghi.
- Ghi bằng `saveAll` và `flush` theo transaction all-or-nothing.
- Import lớn dùng Hibernate JDBC batching và chỉ ghi một activity tổng hợp.
- Xuất dữ liệu lớn dùng streaming workbook.

## Concurrency

- Kanban dùng `@Version`, pessimistic lock Sprint/cột và reorder toàn cột trong một transaction.
- Time Log dùng PostgreSQL transaction advisory lock theo `userId + workDate`.
- Create kiểm tra daily limit sau lock.
- Update khóa ngày cũ và ngày mới theo thứ tự tăng dần trước khi kiểm tra lại daily limit.
- Delete khóa ngày của bản ghi trước khi soft delete.
- Daily limit hiện tại là 720 phút/ngày.

## Kiểm thử còn lại cho Phase 15

- Integration test hai request Time Log chạy thật trên PostgreSQL/Testcontainers.
- Integration test hai client kéo Task đồng thời vào cùng cột Kanban.
- Kiểm tra cache miss/hit và invalidation với Redis thật.
- Đo truy vấn SQL cho các trang Task, Comment, Activity, Notification và Dashboard.
