
import todosAll from './todos-list-all.js'
import todosDetail from './todos-list-detail.js'

function init() {
  todosAll.fetchAllLists();
  todosDetail.resetRenderList();
  todosDetail.bindModalItemEdit();
}

init();

