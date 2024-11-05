package company.mas;

public class DeveloperAgent extends EmployeePaidPerProjectAgent {
    protected void setup() {
        super.setup("Developer");
        
        // Register the agent to the DF
        registerToDF();
    }
}
