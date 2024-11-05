package company.mas;

public class UiUxAgent extends EmployeePaidPerProjectAgent {
    protected void setup() {
        super.setup("UiUx");
        
        // Register the agent to the DF
        registerToDF();
    }
}
