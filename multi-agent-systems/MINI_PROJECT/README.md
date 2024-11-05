# Outsourcing Company Simulator

This mini-project aims to simulate a real outsourcing company (on a basic level). It can be used to handle clients’ projects, assign them to employees and send salaries or payments (based on the work the employee did).

## Types of agents:

**CeoAgent (arguments: initial money, number of UI/UX, Developers, and QA employees to start the company with):**

The simulation of the company starts with the creation of this agent. First, he creates the HrAgent and ManagerAgent. After that, he will create UiUxAgents, DeveloperAgents, and QaAgents based on the arguments provided to him. Every 30 seconds (1 second equals 1 day in the simulation, so approximately every “month”) the CeoAgent will try to send a salary to the HrAgent and to the ManagerAgent. In the case the CeoAgent does not have enough money he will not send the salaries.

**HrAgent (no arguments):**

The role of this agent is to listen to hire requests coming from the ManagerAgent. In the case of a hire request the HrAgent will create (will “hire”) the necessary amount of UiUxAgents, DeveloperAgents, and QaAgents. After the creation of the agents, it will send back a response to the ManagerAgent.

**ManagerAgent (no arguments):**

The role of this agent is to listen to project requests coming from the ClientAgent. The ManagerAgent sends back an accept or a refuse message to the client (depending on the type of the project, the workload, and the client’s budget). In the case the project is accepted: if there is any money left after paying the employees who will work on the project, send the rest of the money to the CeoAgent then verify if there are enough UiUxAgents, DeveloperAgents, and QaAgents to start the specific project. If not, the ManagerAgent will send a hire request to the HrAgent and waits for the response. After he ensured that there are enough employees to start the project he sends them the project and the money they receive for contributing to that specific project.

**UiUxAgent (no arguments):**

This agent can accept project requests from the ManagerAgent. Every time he accepts a project he gets money for it (so his money will grow over time). During the working period (which depends on the workload the client has provided) he is deregistered from the DF, meaning that he can not be assigned to another project until he has not finished with the current one. Does the work of a UI/UX employee.

**DeveloperAgent (no arguments):**

Same as for the UiUxAgent, the only difference is that it does the work of a Developer employee.

**QaAgent (no arguments):**

Same as for the UiUxAgent, the only difference is that it does the work of a Quality Assurance employee.

**ClientAgent (arguments: name of the project, type of the project, workload, budget):**

This agent can send a project request to the ManagerAgent. The ManagerAgent’s decision (to accept or to refuse the project) is sent back as a response to the request.

## Notes:

There are two categories of employees:
1. The ones that receive monthly (fixed) salaries, despite how many projects were accepted by the company in that “month”. These agents are the HrAgent and the ManagerAgent (extended from the EmployeePaidMonthlyAgent abstract class). Can only exist one of each type in a company (one HrAgent and one ManagerAgent).
2. The ones that receive payments based on the workload of the projects they were assigned to. These agents are the UiUxAgents, DeveloperAgents, and QaAgents (extended from the EmployeePaidPerProjectAgent abstract class). Can exist an “infinite” number of them in a company.

There is no limit to the maximum workload an agent (which is paid per project) can take but the higher the workload is the more time he will be busy working on that project (the higher the payment will be too). For example, in case it takes a workload of 60 (60 seconds or 2 “months”) the agents who are paid monthly (the HrAgent and the ManagerAgent) will take 2 salaries in the meantime.

Although any type of agent can be created “manually”, to get the maximum out of the simulation and to recreate the normal workflow of an outsourcing company it is recommended to create firstly the CeoAgent and then add the ClientAgents one after another. All the other agents are created by the CeoAgent or the HrAgent automatically in need. The name of the CeoAgent should start with “Ceo” and the ClientAgent with “Client”!

## Tests and results:

### 1. Scenario
**Steps to reproduce:**

create CeoAgent with args: 1000,0,0,0

**Results**:

The HrAgent and ManagerAgent will not receive salaries (because the CeoAgent does not have enough money).

### 2. Scenario
**Steps to reproduce:**

create CeoAgent with args: 1500,0,0,0

**Results**:

In the first month, the HrAgent will receive a salary but the ManagerAgent will not.

In the following months, none of them will receive a salary.

### 3. Scenario
**Steps to reproduce:**

create CeoAgent with args: 4500,0,0,0

**Results**:

In the first month, the HrAgent and ManagerAgent will receive salaries.

In the following months, none of them will receive a salary.

### 4. Scenario
**Steps to reproduce:**

create CeoAgent with any args (for example: 9000,2,1,1)

create ClientAgent with args: Photograhies,PresentationSite,20,5399

**Results**:

(In the first 2 months the HrAgent and ManagerAgent will receive salaries - not an important result for this test.)

ClientAgent’s project request is refused because the budget for it is not enough.

### 5. Scenario
**Steps to reproduce:**

create CeoAgent with any args (for example: 9000,2,1,1)

create ClientAgent with args: Photograhies,PresentationApp,20,5400

**Results**:

(In the first 2 months the HrAgent and ManagerAgent will receive salaries - not an important result for this test.)

ClientAgent’s project request is refused because the project type was not recognized.

### 6. Scenario
**Steps to reproduce:**

create CeoAgent with any args (for example: 9000,2,1,1)

create ClientAgent with args: Photograhies,PresentationSite,20,5400

**Results**:

(In the first 2 months the HrAgent and ManagerAgent will receive salaries - not an important result for this test.)

ClientAgent’s project request is accepted (because the budget for it is enough and the project type was recognized).

No money is sent to the CeoAgent because the budget was only enough to pay the employees working on the project.

### 7. Scenario
**Steps to reproduce:**

create CeoAgent with any args (for example: 9000,2,1,1)

create ClientAgent with args: Photograhies,PresentationSite,20,5500

**Results**:

(In the first 2 months the HrAgent and ManagerAgent will receive salaries - not an important result for this test.)
ClientAgent’s project request is accepted.

100$ is sent to the CeoAgent.

### 8. Scenario
**Steps to reproduce:**

create CeoAgent with args: 9000,0,0,1

create ClientAgent with args: Photograhies,PresentationSite,20,5400

**Results**:

(In the first 2 months the HrAgent and ManagerAgent will receive salaries - not an important result for this test.)

ClientAgent’s project request is accepted.

The HrAgent will create 2 UiUx Agents and 1 DeveloperAgent based on a request from the ManagerAgent.

### 9. Scenario
**Steps to reproduce:**

create CeoAgent with args: 9000,2,1,1

create ClientAgent with args: Photograhies,PresentationSite,20,5400

within 10-15 seconds create another ClientAgent (with another name and) with args: Photograhies2,PresentationSite,20,5400

**Results**:

(In the first 2 months the HrAgent and ManagerAgent will receive salaries - not an important result for this test.)

Both ClientAgents’ project requests are accepted.

The HrAgent will create 2 UiUxAgents, 1 DeveloperAgent, and 1 QaAgent based on a request from the ManagerAgent because the first 4 employees are still busy working on the first client’s project.

### 10. Scenario
**Steps to reproduce:**

create CeoAgent with args: 0,2,1,1

wait 30 seconds then create ClientAgent with args: Photograhies,PresentationSite,20,9900

**Results**:

In the first month, the HrAgent and ManagerAgent will not receive salaries.

ClientAgent’s project request is accepted.

4500$ is sent to the CeoAgent.

In the second month, the HrAgent and ManagerAgent will receive salaries (in the following months they will not).
