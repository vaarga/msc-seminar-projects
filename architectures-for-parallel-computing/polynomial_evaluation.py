coefficients = [2, 3, 4, 5]
input_list = [10, 25, 21]
output_list = []

P = len(coefficients)
N = len(input_list)

start_from_processor = 0
processors_states = [{ 'p': 0, 'p_apo': 0, 'x': 0, 'x_apo': 0, 'a_n_i': coefficient } for coefficient in coefficients]

print('Coefficients:', coefficients)
print('Input:', input_list)
print('Step 0:')
print(processors_states)

def get_processor_new_state(p, x, a_n_i):
	return { 'p': p, 'p_apo': p * x + a_n_i, 'x': x, 'x_apo': x, 'a_n_i': a_n_i }

for step in range((P - 1) * 2 + N - P + 1):
	popped_input_nr = 0

	if len(input_list) != 0:
		popped_input_nr = input_list.pop(0)
	else:
		start_from_processor += 1

	for i in reversed(range(start_from_processor, P)):
		p = 0
		x = popped_input_nr

		if i != 0:
			p = processors_states[i - 1]['p_apo']
			x = processors_states[i - 1]['x_apo']

		processors_states[i] = get_processor_new_state(p, x, processors_states[i]['a_n_i'])

		if i == P - 1 and step >= (P - 1):
			output_list.append(processors_states[i]['p_apo'])

	print(f'Step {step + 1}:')
	print(processors_states)

print('Output:', output_list)
