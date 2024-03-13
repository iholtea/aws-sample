import todosXhr from './todos-xhr.js'


function renderNewListForm() {
  const mainContainer = document.getElementById('main-container');
  mainContainer.innerHTML = '';

  const todoForm = document.createElement('form');
  todoForm.id = 'todo-form-add';
  mainContainer.appendChild(todoForm);

  todoForm.appendChild( createAddFormHeader() );
  todoForm.appendChild( createAddTitle() );
  todoForm.appendChild( createAddItems() );
  todoForm.appendChild( createAddSubmit() );

}

function createAddFormHeader() {
  const divEl = document.createElement('div');
  divEl.classList.add('row');
  divEl.classList.add('mb-3');
  divEl.innerHTML = '<h5>Create new TODO list</h5>';
  return divEl;
}

function createAddTitle() {
  
  const divEl = document.createElement('div');
  divEl.classList.add('row');
  divEl.classList.add('mb-3');   

  const labelEl = document.createElement('label');
  labelEl.classList.add('col-sm-2');
  labelEl.classList.add('col-form-label'); 
  labelEl.innerHTML='Title:'
  divEl.appendChild(labelEl);

  const innerDiv = document.createElement('div');
  innerDiv.classList.add('col-sm-8');
  innerDiv.classList.add('col-md-6');
  
  const titleInput = document.createElement('input');
  titleInput.type = 'text';
  titleInput.id = 'list-title-input';
  titleInput.name = 'list-title-input';
  titleInput.classList.add('form-control');

  innerDiv.appendChild(titleInput);
  divEl.appendChild(innerDiv);

  return divEl;
}

function createAddItems() {
  const divEl = document.createElement('div');
  divEl.classList.add('row');
  divEl.classList.add('mb-3');   

  const labelEl = document.createElement('label');
  labelEl.classList.add('col-sm-2');
  labelEl.classList.add('col-form-label'); 
  labelEl.innerHTML='Items:'
  divEl.appendChild(labelEl);

  const innerDiv = document.createElement('div');
  innerDiv.classList.add('col-sm-8');
  innerDiv.classList.add('col-md-6');

  for (let i = 1; i <=6; i++) {
    const itemInput = document.createElement('input');
    itemInput.type = 'text';
    itemInput.id = `item-text-input-${i}`;
    itemInput.name = `item-text-input-${i}`;
    itemInput.classList.add('form-control');
    innerDiv.appendChild(itemInput);  
  }

  divEl.appendChild(innerDiv);
  return divEl;

}

function createAddSubmit() {
  const divEl = document.createElement('div');
  divEl.classList.add('row');
  divEl.classList.add('mb-3');
  const innerDiv = document.createElement('div');
  
  const btn = document.createElement('button');
  btn.id = 'btn-add-list';
  btn.classList.add('btn');
  btn.classList.add('btn-primary');
  btn.innerHTML='Submit';
  btn.addEventListener('click', submitNewList);

  innerDiv.appendChild(btn);
  divEl.appendChild(innerDiv);
  return divEl;
  
}

function submitNewList(event) {

  event.preventDefault();
  console.log('submit new form');

  const todoData = {};
  todoData.title = document.getElementById('list-title-input').value;
  
  const allItems = [];
  for( let i=1; i<=6; i++ ) {
    const itemText = document.getElementById(`item-text-input-${i}`).value;
    if( itemText !== null && itemText.trim().length>0 ) {
      const item = {"text": itemText.trim() };
      allItems.push(item);
    }
  }
  todoData.items = allItems;

  todosXhr.addTodoList( todoData, addListCallback );

}

function addListCallback(err, data) {
  if(!err) {
    console.log('new list added');
  }
}

export default {
  renderNewListForm
}
