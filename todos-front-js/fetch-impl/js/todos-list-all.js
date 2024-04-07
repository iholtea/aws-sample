import globalData from './global-data.js'
import todosDetail from './todos-list-detail.js'
import todosAdd from './todos-list-add.js'



async function fetchAllLists() {
  
  const fetchOptions =  {
    method: 'GET',
    headers: {
      'x-api-key': globalData.apiKey
    }  
  };
  try {  
    const response = await fetch(globalData.baseUrl, fetchOptions);
    const receivedTodos = await response.json();
    parseAllLists(receivedTodos)
    renderAllLists(globalData.todos);
  } catch(error) {
    displayAllListsErr(error.message);
  }

}

// obj['state'] = 'Mumbai' - this modifies the existing state property of obj
// obj.state = 'Mumbai' - this should add the state property ot obj.
function parseAllLists(receivedTodos) {
  receivedTodos.forEach( (receivedTodo) => {
    const todo = {};
    todo.uuid = receivedTodo.uuid;
    todo.title = receivedTodo.title;
    todo.creationDate = receivedTodo.creationDate;
    todo.lastViewDate = receivedTodo.lastViewDate;
    todo.extraInfo = receivedTodo.extraInfo;
    todo.items = new Map();
    globalData.todos.set(todo.uuid,todo);
  });
}

function renderAllLists(todos) {
  
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = '';
  
  renderHeader(todosContainer);

  // sort TodoLists by CreationDate reversed
  const todosArray = Array.from( todos.values() );
  todosArray.sort( (item, other) => {
    if ( item.creationDate < other.creationDate ) {
      return 1;
    }
    if( item.creationDate > other.creationDate ) {
      return -1;
    }
    return 0;
  });

  todosArray.forEach( todoList => {
    
    const rowDiv = document.createElement('div');
    rowDiv.classList.add('row');
    
    const textColDiv = document.createElement('div');
    textColDiv.classList.add('col');

    const iconStar = document.createElement('i');
    iconStar.classList.add('fa-solid');
    iconStar.classList.add('fa-splotch');
    textColDiv.appendChild(iconStar);

    const linkEl = document.createElement('a');
    linkEl.id = `list:disp:${todoList.uuid}`;
    linkEl.href = '#';
    linkEl.classList.add('todo-list-display');
    linkEl.innerHTML = `${todoList.title}`;
    textColDiv.appendChild(linkEl);
    
    rowDiv.appendChild(textColDiv);

    const delColDiv = document.createElement('div');
    delColDiv.classList.add('col');

    const delIcon = document.createElement('a');
    delIcon.href = '#';
    delIcon.classList.add('fa-regular');
    delIcon.classList.add('fa-trash-can');
    delIcon.classList.add('list-del-icon');
    delIcon.id = `list:del:${todoList.uuid}`;
    
    delColDiv.appendChild(delIcon);

    rowDiv.appendChild(delColDiv);

    todosContainer.appendChild(rowDiv);
    
  });
  
  bindListsRenderActions();
  bindListsDeleteActions();

  renderNewBtn(todosContainer);
}

function renderHeader(todosContainer) {
  const rowDiv = document.createElement('div');
  rowDiv.classList.add('row');
  rowDiv.classList.add('div-all-header');
  const colDiv = document.createElement('div');
  colDiv.classList.add('col');
  colDiv.innerHTML='<h4>TODO lists</h4>';
  rowDiv.appendChild(colDiv);
  todosContainer.appendChild(rowDiv);
}

// button to create new Todo List
function renderNewBtn(todosContainer) {
  
  const rowDiv = document.createElement('div');
  rowDiv.classList.add('row');
  rowDiv.classList.add('div-new-list-btn');
  
  const colDiv = document.createElement('div');
  colDiv.classList.add('col');

  const newBtn = document.createElement('button');
  newBtn.type = 'button';
  newBtn.classList.add('btn');
  newBtn.classList.add('btn-primary');
  newBtn.innerHTML = 'New TODO list';
  newBtn.addEventListener('click', todosAdd.renderNewListForm);

  colDiv.appendChild(newBtn);
  rowDiv.appendChild(colDiv);
  todosContainer.appendChild(rowDiv);

}

function displayAllListsErr(message) {
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = '';
  renderHeader(todosContainer);

  const rowDiv = document.createElement('div');
  rowDiv.classList.add('row');
  const colDiv = document.createElement('div');
  colDiv.classList.add('col');
  colDiv.innerHTML = `<h5>${message}</h5>`;
  rowDiv.appendChild(colDiv);
  todosContainer.appendChild(rowDiv);

  renderNewBtn(todosContainer);
}

////////////////////////////////////////////////////////////////////////////////////
///////////////////////// View Todo List details and items /////////////////////////
////////////////////////////////////////////////////////////////////////////////////

function bindListsRenderActions() {
  document.querySelectorAll('.todo-list-display').forEach( elem => {
    elem.addEventListener('click', processRenderListEvent);
  });
}


/*
* List of todo-s is made of elements like
* <a id="list:disp:${todoList.uuid}" class="todo-list-display">Todo Title</a></li>
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

////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Delete Todo List section   ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////////

function bindListsDeleteActions() {
  document.querySelectorAll('.list-del-icon').forEach( elem => {
    elem.addEventListener('click', processDeleteListEvent);
  }); 
}

function processDeleteListEvent(event) {
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoListUuid = targetElem.id.split(':')[2];
  const todoTitle = globalData.todos.get(todoListUuid).title;
  if( confirm(`Are you sure you want to delete TODO: ${todoTitle}`) ) {
    globalData.currentTodoUuid = todoListUuid;
    deleteListByUuid(todoListUuid)
  }
}

async function deleteListByUuid(todoListUuid) {
  const todoListUrl = `${globalData.baseUrl}/${todoListUuid}`; 
  const fetchOptions =  {
    method: 'DELETE',
    headers: {
      'x-api-key': globalData.apiKey
    }  
  };
  try {
    const response = await fetch(todoListUrl, fetchOptions);
    if(response.ok) {
      globalData.todos.delete(globalData.currentTodoUuid);
      globalData.currentTodoUuid = null;
      renderAllLists(globalData.todos);
      todosDetail.resetRenderList();
    } else {
      console.log('Error calling backed service');   
    }
  } catch(error) {
    console.log(error.message);
  }
   
}


export default {
  fetchAllLists,
  renderAllLists
}