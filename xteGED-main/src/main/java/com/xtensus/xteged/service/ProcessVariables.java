package com.xtensus.xteged.service;

public class ProcessVariables {
    private String bpm_assignee;
    private boolean bpm_sendEMailNotifications;
    private int bpm_workflowPriority;

    public String getBpm_assignee() {
        return bpm_assignee;
    }

    public void setBpm_assignee(String bpm_assignee) {
        this.bpm_assignee = bpm_assignee;
    }

    public boolean isBpm_sendEMailNotifications() {
        return bpm_sendEMailNotifications;
    }

    public void setBpm_sendEMailNotifications(boolean bpm_sendEMailNotifications) {
        this.bpm_sendEMailNotifications = bpm_sendEMailNotifications;
    }

    public int getBpm_workflowPriority() {
        return bpm_workflowPriority;
    }

    public void setBpm_workflowPriority(int bpm_workflowPriority) {
        this.bpm_workflowPriority = bpm_workflowPriority;
    }
}
