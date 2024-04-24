
import todosAll from './todos-list-all.js'
import todosDetail from './todos-list-detail.js'

async function init() {
  todosDetail.resetRenderList();
  todosDetail.bindModalItemEdit();
  // wait for fetch all before re-set the detail div
  await todosAll.fetchAllLists();
}

init();

