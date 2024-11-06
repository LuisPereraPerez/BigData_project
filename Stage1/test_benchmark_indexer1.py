import pytest
import shutil
import os
from indexer1 import execute, update_last_book_id

@pytest.fixture(scope="function")
def setup_teardown():
    # Preparación: Borrar el directorio "datamart" y reiniciar el archivo de ID
    shutil.rmtree("datamart", ignore_errors=True)  # Eliminar el datamart si existe
    update_last_book_id("resources/lastBookId.txt", 0, 1)  # Reiniciar el archivo de ID
    yield
    # Aquí podrías hacer algún tipo de limpieza adicional si es necesario

def test_execute_benchmark(benchmark, setup_teardown):
    # Benchmarking de la función execute
    benchmark(execute)
