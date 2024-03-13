import todosXhr from './todos-xhr.js'
import globalData from './global-data.js'
import todosDetail from './todos-list-detail.js'
import todosAdd from './todos-list-add.js'

function fetchAllLists() {
  todosXhr.fetchAllLists( fetchAllListsCallback );
}

function fetchAllListsCallback(err, data, status) {
  if(!err) {
    parseAllLists(data)
    renderAllLists(globalData.todos);
  } else if( err === 'ERR_HTTP_STATUS' ) {
    todosUi.displayAllListsMessage(`Http Status ${status} received`);    
  } else if( err === 'ERR_OTHER' ) {
    todosUi.displayAllListsMessage('Cannot fetch TODO lists');
  }
}

// obj['state'] = 'Mumbai' - this modifies the existing state property of obj
// obj.state = 'Mumbai' - this should add the state property ot obj.
// rename this to parseFetchedAllLists
function parseAllLists(receivedData) {
  const receivedTodos = JSON.parse(receivedData);
  receivedTodos.forEach( (receivedTodo) => {
    const todo = {};
    todo.uuid = receivedTodo.uuid;
    todo.title = receivedTodo.title;
    todo.creationDate = receivedTodo.creationDate;
    todo.lastUpdate = receivedTodo.lastUpdate;
    todo.extraInfo = receivedTodo.extraInfo;
    todo.items = new Map();
    globalData.todos.set(todo.uuid,todo);
  });
}

function renderAllLists(todos) {
  
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = '';
  const ul = document.createElement('ul');
  todosContainer.appendChild(ul);

  todos.forEach( (todoList, key) => {
    
    // compute how many items are done
    /*
    const doneNr = todoList.items.reduce( (result, item) => {
      return item.done ? result+1 : result;
    }, 0);
    */
    
    const li = document.createElement('li');
    const liLink = document.createElement('a');
    liLink.id = `todo:list:${todoList.uuid}`;
    liLink.href = '#';
    liLink.className = 'todo-list-display';
    liLink.innerHTML = `${todoList.title}`;
    li.appendChild(liLink);
    
    ul.appendChild(li);
  });
  
  
  bindListsRenderActions()
}

function bindListsRenderActions() {
  document.querySelectorAll('.todo-list-display').forEach( elem => {
    elem.addEventListener('click', processRenderListEvent);
  });
}

/*
* List of todo-s is made of elements like
* <li><a id="todo:list:${todoList.uuid}" class="todo-list-display">Todo Title</a></li>
*
* When click-ing on a todo list -> display it and its elements
*/
function processRenderListEvent(event) {
  
  // get the uuid of the TodoList that was clicked(selected)
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoListUuid = targetElem.id.split(':')[2];
  
  // set current TodoList to the selected one
  globalData.currentTodoUuid = todoListUuid;
  
  todosDetail.fetchList(todoListUuid);
  
}

function bindBtnNewListEvent() {
  const btnNewList = document.getElementById('btn-new-list');
  btnNewList.addEventListener('click', todosAdd.renderNewListForm);
}

function displayAllListsMessage(message) {
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = `<h5>${message}</h5>`;
}


export default {
  fetchAllLists,
  bindBtnNewListEvent
}