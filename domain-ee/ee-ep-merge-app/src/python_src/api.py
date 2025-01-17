import asyncio
import logging
import sys
from contextlib import asynccontextmanager
from uuid import UUID, uuid4

import uvicorn
from fastapi import BackgroundTasks, FastAPI, HTTPException, status
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from model.merge_job import MergeJob
from pydantic_models import (MergeEndProductsErrorResponse,
                             MergeEndProductsRequest, MergeEndProductsResponse)
from service.ep_merge_machine import EpMergeMachine
from service.hoppy_service import HOPPY
from service.job_store import JobStore
from util.sanitizer import sanitize

job_store = JobStore()


@asynccontextmanager
async def lifespan(api: FastAPI):
    await on_start_up()
    yield
    await on_shut_down()


async def on_start_up():
    await HOPPY.start_hoppy_clients(asyncio.get_event_loop())


async def on_shut_down():
    HOPPY.stop_hoppy_clients()


app = FastAPI(
    title="EP Merge Tool",
    description="Merge EP400 claim contentions into a pending claim (EP 010/020/110).",
    contact={},
    version="v0.1",
    license={
        "name": "CCO 1.0",
        "url": "https://github.com/department-of-veterans-affairs/abd-vro/blob/master/LICENSE.md"
    },
    servers=[
        {
            "url": "/ep",
            "description": "",
        },
    ],
    lifespan=lifespan
)

logging.basicConfig(
    format="[%(asctime)s] %(levelname)-8s %(message)s",
    level=logging.INFO,
    datefmt="%Y-%m-%d %H:%M:%S",
    stream=sys.stdout,
)


@app.get("/health")
def get_health_status():
    return {"status": "ok"}


@app.post("/merge",
          status_code=status.HTTP_202_ACCEPTED,
          response_model=MergeEndProductsResponse,
          response_model_exclude_none=True)
async def merge_claims(merge_request: MergeEndProductsRequest, background_tasks: BackgroundTasks):
    validate_merge_request(merge_request)

    job_id = uuid4()
    logging.info(f"event=mergeJobSubmitted "
                 f"job_id={job_id} "
                 f"pending_claim_id={sanitize(merge_request.pending_claim_id)} "
                 f"ep400_claim_id={sanitize(merge_request.ep400_claim_id)}")

    merge_job = MergeJob(job_id=job_id,
                         pending_claim_id=merge_request.pending_claim_id,
                         ep400_claim_id=merge_request.ep400_claim_id)
    job_store.submit_merge_job(merge_job)

    background_tasks.add_task(start_job_state_machine, merge_job)

    return jsonable_encoder({"job": merge_job})


def validate_merge_request(merge_request: MergeEndProductsRequest):
    if merge_request.pending_claim_id == merge_request.ep400_claim_id:
        raise HTTPException(status_code=400, detail="Claim IDs must be different.")


def start_job_state_machine(merge_job):
    EpMergeMachine(merge_job).process()


@app.get("/merge/{job_id}",
         response_model=MergeEndProductsResponse,
         responses={
             status.HTTP_200_OK: {"description": "Found job by job_id"},
             status.HTTP_404_NOT_FOUND: {"model": MergeEndProductsErrorResponse,
                                         "description": "Could not find job by job_id"}
         },
         response_model_exclude_none=True)
async def get_merge_claims_status(job_id: UUID):
    job = job_store.get_merge_job(job_id)
    if job:
        logging.info(f"event=getMergeStatus {job}")
        return jsonable_encoder({"job": job})
    else:
        return JSONResponse(status_code=status.HTTP_404_NOT_FOUND,
                            content=jsonable_encoder(
                                {"job_id": job_id,
                                 "message": "Could not find job"}))


@app.get("/merge")
async def get_all_merge_jobs():
    return jsonable_encoder({"jobs": list(job_store.get_merge_jobs())})


if __name__ == "__main__":
    uvicorn.run(app, host="localhost", port=8140)
