import json
import uuid
from unittest.mock import AsyncMock, Mock

import pytest
from hoppy.exception import ResponseException
from src.python_src.model import (cancel_claim, get_contentions,
                                  update_contentions)
from src.python_src.model import update_temp_station_of_jurisdiction as tsoj
from src.python_src.service.contentions_util import MergeException
from src.python_src.service.ep_merge_machine import EpMergeMachine
from src.python_src.service.merge_job import JobState, MergeJob

RESPONSE_DIR = './tests/service/responses'
response_200 = f'{RESPONSE_DIR}/200_response.json'
response_404 = f'{RESPONSE_DIR}/404_response.json'
response_400 = f'{RESPONSE_DIR}/400_response.json'
response_500 = f'{RESPONSE_DIR}/500_response.json'
pending_contentions_200 = f'{RESPONSE_DIR}/get_pending_claim_contentions_200.json'
supp_contentions_200 = f'{RESPONSE_DIR}/get_supp_claim_contentions_200.json'


def load_response(file, response_type):
    with open(file) as f:
        return response_type.model_validate(json.load(f))


get_pending_contentions_200 = load_response(pending_contentions_200, get_contentions.Response)
get_supp_contentions_200 = load_response(supp_contentions_200, get_contentions.Response)
update_temporary_station_of_duty_200 = load_response(response_200, tsoj.Response)
update_pending_claim_200 = load_response(response_200, update_contentions.Response)
cancel_claim_200 = load_response(response_200, cancel_claim.Response)


@pytest.fixture(autouse=True)
def mock_hoppy_async_client(mocker):
    return mocker.patch('hoppy.async_hoppy_client.RetryableAsyncHoppyClient')


@pytest.fixture(autouse=True)
def mock_hoppy_service(mocker):
    return mocker.patch('src.python_src.service.hoppy_service.HoppyService')


@pytest.fixture(autouse=True)
def mock_hoppy_service_get_client(mock_hoppy_service, mock_hoppy_async_client):
    mock_hoppy_service.get_client.return_value = mock_hoppy_async_client


def test_constructor(mock_hoppy_service):
    merge_job = Mock()
    machine = EpMergeMachine(mock_hoppy_service, merge_job)

    assert machine.current_state_value == JobState.PENDING


@pytest.fixture
def machine(mock_hoppy_service):
    return EpMergeMachine(mock_hoppy_service,
                          MergeJob(job_id=uuid.uuid4(),
                                   pending_claim_id=1,
                                   supp_claim_id=2))


def get_mocked_async_response(side_effects):
    mock = AsyncMock()
    mock.side_effect = side_effects
    return mock


def mock_async_responses(mock_hoppy_async_client, responses):
    mock = AsyncMock()
    mock.side_effect = responses
    mock_hoppy_async_client.make_request = mock


def process_and_assert(machine, expected_state, expected_error_state):
    machine.process()
    assert machine.current_state_value == expected_state
    assert machine.job.state == expected_state
    assert machine.job.error_state == expected_error_state


@pytest.mark.parametrize("invalid_request",
                         [
                             pytest.param(ResponseException("Oops"), id="Caught Exception"),
                             pytest.param(load_response(response_400, get_contentions.Response), id="400"),
                             pytest.param(load_response(response_404, get_contentions.Response), id="404"),
                             pytest.param(load_response(response_500, get_contentions.Response), id="500")
                         ])
def test_invalid_request_at_get_pending_contentions(machine, mock_hoppy_async_client, invalid_request):
    mock_async_responses(mock_hoppy_async_client, invalid_request)
    process_and_assert(machine, JobState.COMPLETED_ERROR, JobState.RUNNING_GET_PENDING_CLAIM_CONTENTIONS)


@pytest.mark.parametrize("invalid_request",
                         [
                             pytest.param(ResponseException("Oops"), id="Caught Exception"),
                             pytest.param(load_response(response_400, get_contentions.Response), id="400"),
                             pytest.param(load_response(response_404, get_contentions.Response), id="404"),
                             pytest.param(load_response(response_500, get_contentions.Response), id="500")
                         ])
def test_invalid_request_at_get_supplemental_contentions(machine, mock_hoppy_async_client, invalid_request):
    mock_async_responses(mock_hoppy_async_client,
                         [
                             get_pending_contentions_200,
                             invalid_request
                         ])
    process_and_assert(machine, JobState.COMPLETED_ERROR, JobState.RUNNING_GET_SUPP_CLAIM_CONTENTIONS)


@pytest.mark.parametrize("invalid_request",
                         [
                             pytest.param(ResponseException("Oops"), id="Caught Exception"),
                             pytest.param(load_response(response_400, tsoj.Response), id="400"),
                             pytest.param(load_response(response_404, tsoj.Response), id="404"),
                             pytest.param(load_response(response_500, tsoj.Response), id="500")
                         ])
def test_invalid_request_at_set_temporary_station_of_duty(machine, mock_hoppy_service, mock_hoppy_async_client,
                                                          invalid_request):
    mock_async_responses(mock_hoppy_async_client,
                         [
                             get_pending_contentions_200,
                             get_supp_contentions_200,
                             invalid_request
                         ])
    process_and_assert(machine, JobState.COMPLETED_ERROR, JobState.RUNNING_SET_TEMP_STATION_OF_JURISDICTION)


def test_process_fails_at_merge_contentions(machine, mock_hoppy_async_client, mocker):
    mock_async_responses(mock_hoppy_async_client,
                         [
                             get_pending_contentions_200,
                             get_supp_contentions_200,
                             update_temporary_station_of_duty_200
                         ])
    mocker.patch('src.python_src.service.ep_merge_machine.ContentionsUtil.merge_claims',
                 side_effect=MergeException(message="Merge Oops"))

    process_and_assert(machine, JobState.COMPLETED_ERROR, JobState.RUNNING_MERGE_CONTENTIONS)


@pytest.mark.parametrize("invalid_request",
                         [
                             pytest.param(ResponseException("Oops"), id="Caught Exception"),
                             pytest.param(load_response(response_400, update_contentions.Response), id="400"),
                             pytest.param(load_response(response_404, update_contentions.Response), id="404"),
                             pytest.param(load_response(response_500, update_contentions.Response), id="500")
                         ])
def test_invalid_request_at_update_contentions(machine, mock_hoppy_async_client, invalid_request):
    mock_async_responses(mock_hoppy_async_client,
                         [
                             get_pending_contentions_200,
                             get_supp_contentions_200,
                             update_temporary_station_of_duty_200,
                             invalid_request
                         ])
    process_and_assert(machine, JobState.COMPLETED_ERROR, JobState.RUNNING_UPDATE_PENDING_CLAIM_CONTENTIONS)


@pytest.mark.parametrize("invalid_request",
                         [
                             pytest.param(ResponseException("Oops"), id="Caught Exception"),
                             pytest.param(load_response(response_400, cancel_claim.Response), id="400"),
                             pytest.param(load_response(response_404, cancel_claim.Response), id="404"),
                             pytest.param(load_response(response_500, cancel_claim.Response), id="500")
                         ])
def test_invalid_request_at_cancel_claim_due_to_exception(machine, mock_hoppy_async_client, invalid_request):
    mock_async_responses(mock_hoppy_async_client,
                         [
                             get_pending_contentions_200,
                             get_supp_contentions_200,
                             update_temporary_station_of_duty_200,
                             update_pending_claim_200,
                             invalid_request
                         ])
    process_and_assert(machine, JobState.COMPLETED_ERROR, JobState.RUNNING_CANCEL_SUPP_CLAIM)


def test_process_succeeds(machine, mock_hoppy_async_client):
    mock_async_responses(mock_hoppy_async_client,
                         [
                             get_pending_contentions_200,
                             get_supp_contentions_200,
                             update_temporary_station_of_duty_200,
                             update_pending_claim_200,
                             cancel_claim_200
                         ])
    process_and_assert(machine, JobState.COMPLETED_SUCCESS, None)