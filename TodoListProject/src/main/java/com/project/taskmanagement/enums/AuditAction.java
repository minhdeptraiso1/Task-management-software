package com.project.taskmanagement.enums;

public enum AuditAction {
    LOGIN,
    LOGOUT,
    CHANGE_PASSWORD,
    LOGOUT_ALL,
    //===============Project==============
    CREATE_PROJECT,
    UPDATE_PROJECT,
    DELETE_PROJECT,
    PROJECT_STATUS_CHANGED,
    
    ADD_PROJECT_MEMBER,
    REMOVE_PROJECT_MEMBER,
    CHANGE_PROJECT_MEMBER_ROLE
}
