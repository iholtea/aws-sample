import todosUi from './todos-ui.js'
import todosXhr from './todos-xhr.js'


/////////  Callback-s  /////////////  

function fetchAllListsCallback(err, data, status) {
  if(!err) {
    const todos = JSON.parse(data);
    todosUi.displayAllLists(todos);
    bindListsDisplayActions();
  } else if( err === 'ERR_HTTP_STATUS' ) {
    todosUi.displayAllListsMessage(`Http Status ${status} received`);    
  } else if( err === 'ERR_OTHER' ) {
    todosUi.displayAllListsMessage('Cannot fetch TODO lists');
  }
}

function displayListCallback(err, data) {
  if(!err) {
    const todo = JSON.parse(data);
    todosUi.displayList(todo);
    bindListsDisplayActions();
  } else {
    console.log('Error calling backed service');
  }  
}

/////////////////////////////////////////////////


function bindListsDisplayActions() {
  document.querySelectorAll('.todo-list-display').forEach( elem => {
    elem.addEventListener('click', processDisplayListEvent);
  });
}

/*
* List of todo-s is made of elements like
* <li><a id="todo:list:${todoList.uuid}" class="todo-list-display">Todo Title</a></li>
*
* When click-ing on a todo list -> display it and its elements
*/
function processDisplayListEvent(event) {
  
  // get the uuid of the TodoList that was clicked(selected)
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoListUuid = targetElem.id.split(':')[2];
  todosXhr.fetchListById(todoListUuid, displayListCallback);
  
}

function bindBtnNewListEvent() {
  const btnNewList = document.getElementById('btn-new-list');
  btnNewList.addEventListener('click', todosUi.displayNewListForm);
}

function init() {
  todosXhr.fetchAllLists( fetchAllListsCallback );
  bindBtnNewListEvent();
}

init();

