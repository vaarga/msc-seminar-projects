package company.mas;

public class QaAgent extends EmployeePaidPerProjectAgent {
    protected void setup() {
        super.setup("Qa");
        
        // Register the agent to the DF
        registerToDF();
    }
}
