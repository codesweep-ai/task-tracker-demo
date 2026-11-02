package com.acme.tracker.model;

public class Task {
    private long id;
    private String title;
    private String description;
    private String status;       // OPEN | IN_PROGRESS | PENDING_APPROVAL | DONE
    private String priority;     // LOW | MEDIUM | HIGH
    private Long assigneeId;     // nullable
    private String assigneeName; // transient, joined for display
    private long projectId;
    private String projectName;  // transient, joined for display
    private String dueDate;      // ISO yyyy-MM-dd, nullable
    private String updatedAt;    // yyyy-MM-dd HH:mm:ss, refreshed on every write

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
