import csv
import os
from abc import ABC

from .condition_dropdown_table_version import CONDITION_DROPDOWN_TABLE_VERSION
from .diagnostic_code_table_version import TABLE_VERSION

# https://docs.google.com/spreadsheets/d/18Mwnn9-cvJIRRupQyQ2zLYOBm3bd0pr4kKlsZtFiyc0/edit#gid=1711756762
dc_table_name = f"Contention Classification Diagnostic Codes Lookup table master sheet - DC Lookup {TABLE_VERSION}.csv"
# https://docs.google.com/spreadsheets/d/1A5JuYwn39mHE5Mk1HazN-mxCL2TENPeyUPHHhH10g_I/edit#gid=819850041
condition_dropdown_table_name = f"Contention dropdown to classification master - Dropdown Lookup {CONDITION_DROPDOWN_TABLE_VERSION}.csv"


class LookupTable(ABC):
    """Generalized lookup table for mapping input strings to contention classification codes"""

    CSV_FILEPATH = None
    input_key = None
    output_key = None

    def __init__(self):
        if not self.CSV_FILEPATH:
            raise NotImplementedError("csv_filepath must be set in child class")
        self.mappings = get_lookup_table(
            self.CSV_FILEPATH, input_key=self.input_key, output_key=self.output_key
        )

    def __len__(self):
        return len(self.mappings)

    def get(self, input_str, fallback=None):
        return self.mappings.get(input_str, fallback)


class ConditionDropdownLookupTable(LookupTable):
    """Lookup table for mapping condition dropdown values to contention classification codes"""

    CSV_FILEPATH = os.path.join(
        os.path.dirname(__file__), "data", "condition_dropdown_lookup_table", condition_dropdown_table_name
    )
    input_key = "CONTENTION_TEXT"
    output_key = "CLASSIFICATION_CODE"

    def __init__(self):
        super().__init__()

    def get(self, input_str: str, fallback=None):
        input_str = input_str.strip().lower()
        return self.mappings.get(input_str, fallback)


class DiagnosticCodeLookupTable(LookupTable):
    """Lookup table for mapping diagnostic codes to contention classification codes"""

    CSV_FILEPATH = os.path.join(
        os.path.dirname(__file__), "data", "dc_lookup_table", dc_table_name
    )
    input_key = "DIAGNOSTIC_CODE"
    output_key = "CLASSIFICATION_CODE"

    def __init__(self):
        super().__init__()


def get_lookup_table(filepath, input_key, output_key):
    classification_code_mappings = {}
    with open(filepath, "r") as fh:
        csv_reader = csv.DictReader(fh)
        for csv_line in csv_reader:
            try:
                try:
                    text_to_convert = int(csv_line[input_key])
                except ValueError:
                    text_to_convert = csv_line[input_key].lower()
                classification_code = int(csv_line[output_key])
                classification_code_mappings[text_to_convert] = classification_code
            except KeyError:
                print(f"csv_line: {csv_line}")
                raise

    return classification_code_mappings
