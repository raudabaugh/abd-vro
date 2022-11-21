import pytest

from assessclaimdc6602v2.src.lib import main


@pytest.mark.parametrize(
    "request_body, response",
    [
        (
                {
                    "evidence": {
                        "medications": [
                            {
                                "description": "Prednisone",
                                "status": "active",
                                "authoredOn": "1952-04-06T04:00:00Z",
                            }
                        ],
                        "conditions": [],
                    },
                    "date_of_claim": "2021-11-09",
                },
                {"evidence": {"conditions": [],
                              "medications": [{"authoredOn": "1952-04-06T04:00:00Z",
                                               "conditionRelated": "true",
                                               "description": "Prednisone",
                                               "status": "active",
                                               "suggestedCategory": [
                                                   "Anti-Inflammatory/Corticosteroid/Immuno-Suppressive"]}]},
                 "evidenceSummary": {"relevantConditionsCount": 0,
                                     "relevantMedCount": 1,
                                     "totalConditionsCount": 0,
                                     "totalMedCount": 1}},
        ),
        # demonstrates ability to match substrings in medication["text"] property
        (
                {
                    "evidence": {
                        "medications": [
                            {
                                "description": "predniSONE 1 MG Oral Tablet",
                                "status": "active",
                                "authoredOn": "1952-04-06T04:00:00Z",
                            }
                        ],
                        "conditions": [],
                    },
                    "date_of_claim": "2021-11-09",
                },
                {"evidence": {"conditions": [],
                              "medications": [{"authoredOn": "1952-04-06T04:00:00Z",
                                               "conditionRelated": "true",
                                               "description": "predniSONE 1 MG "
                                                              "Oral Tablet",
                                               "status": "active",
                                               "suggestedCategory": [
                                                   "Anti-Inflammatory/Corticosteroid/Immuno-Suppressive"]}]},
                 "evidenceSummary": {"relevantConditionsCount": 0,
                                     "relevantMedCount": 1,
                                     "totalConditionsCount": 0,
                                     "totalMedCount": 1}},
        ),
        # calculator feild mild-persistent-asthma-or-greater is True
        (
                {
                    "evidence": {
                        "medications": [
                            {
                                "description": "Advil",
                                "status": "active",
                                "authoredOn": "1952-04-06T04:00:00Z",
                            }
                        ],
                        "conditions": [],
                    },
                    "date_of_claim": "2021-11-09",
                },
                {"evidence": {"conditions": [],
                              "medications": [{"authoredOn": "1952-04-06T04:00:00Z",
                                               "conditionRelated": "false",
                                               "description": "Advil",
                                               "status": "active",
                                               "suggestedCategory": []}]},
                 "evidenceSummary": {"relevantConditionsCount": 0,
                                     "relevantMedCount": 0,
                                     "totalConditionsCount": 0,
                                     "totalMedCount": 1}}
        ),
    ],
)
def test_main(request_body, response):
    """
    Test the function that takes the request and returns the response

    :param request_body: request body with blood pressure readings and other data
    :type request_body: dict
    :param response: response after running data through algorithms
    :type response: dict
    """
    api_response = main.assess_asthma(request_body)

    assert api_response == response