import random
from time import sleep
import os

clear = lambda: os.system('clear')

# Note: The edges of the board will not be displayed
BOARD_WIDTH = 50
BOARD_HEIGHT = 20

board = [[random.randint(0,1) for _ in range(BOARD_WIDTH)] for _ in range(BOARD_HEIGHT)]

def get_cell_representation(cell):
	if cell == 1:
		return bytes((219,)).decode('cp437')

	return ' '

def print_board(board):
	for i in range(1, len(board) - 1):
		for j in range(1, len(board[i]) - 1):
			print(get_cell_representation(board[i][j]), end = '')

		print()

def get_new_cell(old_cell, alive_neighbors_nr):
	# Death because of overpopulation or loneliness
	if old_cell == 1 and (alive_neighbors_nr >= 4 or alive_neighbors_nr <= 1):
		return 0
	# Birth
	elif alive_neighbors_nr == 3:
		return 1

	return old_cell;

def get_new_board(old_board):
	new_board = [[0] * len(old_board[i]) for i in range(len(old_board))]

	for i in range(1, len(old_board) - 1):
		for j in range(1, len(old_board[i]) - 1):
			alive_neighbors_nr = old_board[i - 1][j - 1] \
			+ old_board[i - 1][j] \
			+ old_board[i - 1][j + 1] \
			+ old_board[i][j + 1] \
			+ old_board[i + 1][j + 1] \
			+ old_board[i + 1][j] \
			+ old_board[i + 1][j - 1] \
			+ old_board[i][j - 1] \

			new_board[i][j] = get_new_cell(old_board[i][j], alive_neighbors_nr)

	return new_board

while True:
	clear()

	print_board(board)

	board = get_new_board(board)

	sleep(0.25)
