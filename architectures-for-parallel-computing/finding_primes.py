P = 4
N = 4
input_list = list(range(2, N + 2))
output_list = []
start_from_processor = 0
processors_states = [{ 'p': 0, 'a': 0, 'a_apo': 0 }] * P

print('Input: ', input_list)
print('Step 0:')
print(processors_states)

def get_processor_new_state(p, a):
	new_p = p
	new_a_apo = 0

	if a <= 0:
		new_a_apo = a
	elif p == 0:
		new_p = a
		new_a_apo = -a
	elif a % p == 0:
		new_a_apo = 0
	else:
		new_a_apo = a

	return { 'p': new_p, 'a': a, 'a_apo': new_a_apo }

for step in range((P - 1) * 2 + (N - P + 1)):
	popped_input_nr = 0

	if len(input_list) != 0:
		popped_input_nr = input_list.pop(0)
	else:
		start_from_processor += 1

	for i in reversed(range(start_from_processor, P)):
		a = 0

		# If it's the first processor use the popped number from the input as 'a'
		if i == 0:
			a = popped_input_nr
		else:
			a = processors_states[i - 1]['a_apo']

		processors_states[i] = get_processor_new_state(processors_states[i]['p'], a)

		# If this is the last processor output his 'a_apo'
		if i == P - 1:
			output_list.append(processors_states[i]['a_apo'])

	print(f'Step {step + 1}:')
	print(processors_states)

def format_output(output):
	output_without_zeros = [item for item in output_list if item != 0]

	return [abs(item) for item in output_without_zeros]

print('Output:', format_output(output_list))
