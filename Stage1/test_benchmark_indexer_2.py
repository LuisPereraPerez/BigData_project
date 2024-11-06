import pytest
from indexer2 import execute

@pytest.mark.benchmark
def test_execute_benchmark(benchmark):
    # Benchmark the `execute()` function with '2' as the input to process all files
    benchmark(execute, choice='2')