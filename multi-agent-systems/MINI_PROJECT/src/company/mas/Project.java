package company.mas;

import jade.util.leap.Serializable;

public class Project implements Serializable {
    private String projectName;
    private String projectType;
    private int workload;
    private int budget;

    public Project(String projectName, String projectType, int workload, int budget) {
        this.projectName = projectName;
        this.projectType = projectType;
        this.workload = workload;
        this.budget = budget;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectType() {
        return projectType;
    }
    
    public int getWorkload() {
        return workload;
    }
    
    public int getBudget() {
        return budget;
    }
}
