RULE = 90
GENERATIONS_NR = 9

generation = '0000000001000000000'
one_generation_length = len(generation)

def get_cell_representation(cell):
	if cell == '1':
		return bytes((219,)).decode('cp437')

	return ' '

def print_generation(generation_to_print):
	 for cell in bin(generation_to_print)[2:].zfill(one_generation_length):
	 	print(get_cell_representation(cell), end = '')

	 print()

def get_new_generation(old_generation):
	new_generation = 0

	for pos in range(1, one_generation_length - 1):
		rule_pos = (old_generation & (7 << (pos - 1))) >> (pos - 1)
		new_cell = (RULE & (1 << rule_pos)) >> rule_pos
		new_generation = (new_cell << pos) + new_generation

	return new_generation

# Convert the first (initial) generation
generation = int(generation, 2)

print_generation(generation)

for _ in range(GENERATIONS_NR - 1):
	generation = get_new_generation(generation)

	print_generation(generation)
