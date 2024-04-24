import globalData from './global-data.js'
import todosAll from './todos-list-all.js'
import todosDetail from './todos-list-detail.js'

const loginHtml = `
  <form id="login-form">

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-5">
      <h5>Sign in
    </div> 
    <div class="col-sm-6"> 
      <a id="register-link" href="">Register new account</a></h5>
    </div> 
  </div>

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-3">
      <label class="col-form-label" for="login-email">Email:</label>
    </div>
    <div class="col-sm-8">
      <input type="text" id="login-email" name="login-email" class="form-control"/>
    </div>
  </div>

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-3">
      <label class="col-form-label" for="login-pass">Password:</label>
    </div>
    <div class="col-sm-8">
      <input type="password" id="login-pass" name="login-pass" class="form-control"/>
    </div>
  </div>

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-11">
      <button id="login-btn" class="btn btn-primary">Sign in</button>
    </div> 
  </div>

  </form>
`;

const registerHtml = `
  <form id="register-form">

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-6">
      <h5>Create account</h5> 
    </div>
    <div class="col-sm-5">
      <a id="login-link" href="">Sign in</a>
    </div> 
  </div>

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-4">
      <label class="col-form-label" for="register-email">Email:</label>
    </div>
    <div class="col-sm-7">
      <input type="text" id="register-email" name="login-email" class="form-control"/>
    </div>
  </div>

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-4">
      <label class="col-form-label" for="resgister-pass">Password:</label>
    </div>
    <div class="col-sm-7">
      <input type="password" id="register-pass" name="register-pass" class="form-control"/>
    </div>
  </div>

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-4">
      <label class="col-form-label" for="register-pass-confirm">Confirm Password:</label>
    </div>
    <div class="col-sm-7">
      <input type="password" id="register-pass-confirm" name="register-pass-confirm" class="form-control"/>
    </div>
  </div>

  <div class="row mb-3">
    <div class="col-sm-1">&nbsp;</div>
    <div class="col-sm-11">
      <button id="register-btn" class="btn btn-primary">Create Account</button>
    </div> 
  </div>

  </form>
`;



function displayLogin(event) {
  
  if(event) {
    event.preventDefault();  
  }

  document.getElementById('main-container').innerHTML = '';
  document.getElementById('all-lists-container').innerHTML = loginHtml;
  
  document.getElementById('register-link')
      .addEventListener('click', displayCreateNew);

  document.getElementById('login-btn')
      .addEventListener('click', signIn);
      
  globalData.clearCachedData();    
  
}

function displayCreateNew(event) {
  
  if(event) {
    event.preventDefault();
  }

  document.getElementById('main-container').innerHTML = '';
  document.getElementById('all-lists-container').innerHTML = registerHtml;
  
  document.getElementById('login-link')
      .addEventListener('click', displayLogin);

  document.getElementById('register-btn')
      .addEventListener('click', registerUser);    
  
}

async function signIn(event) {
  
  event.preventDefault();

  const loginRequest = {
    email: document.getElementById('login-email').value,
    password: document.getElementById('login-pass').value
  };
  const fetchOptions = {
    method: 'POST',
    headers: {
      'content-type': 'application/json'  
    },
    body: JSON.stringify(loginRequest)
  };

  try {
    const response = await fetch(globalData.loginUrl, fetchOptions);
    const loginResponse = await response.json();
    if(response.ok) {
      console.log(`Login successful for user ${loginRequest.email}`);
      globalData.jwt = loginResponse.jwt;
      // wait for fetch all before re-set the detail div
      await todosAll.fetchAllLists();
      todosDetail.resetRenderList();
    } else if( response.status === 401 ) {
      console.log(loginResponse.message);
    }
    
  } catch(error) {
    console.log(error.message);
  }

}

function registerUser(event) {
  event.preventDefault();
}

export default {
  displayLogin,
  displayCreateNew,
}

