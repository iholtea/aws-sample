
import todosAll from './todos-list-all.js'

function init() {
  todosAll.fetchAllLists();
  todosAll.bindBtnNewListEvent();
}

init();

