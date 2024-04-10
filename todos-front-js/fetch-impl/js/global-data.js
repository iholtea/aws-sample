const apiKeyHeader = 'x-api-key';

// local SpringBoot REST backend 
const baseUrl = 'http://localhost:8080/todos';
const apiKey = 'dummyKey';

// manual created API Gateway in AWS web console
//const apiKey = 'AGdASLxD8t3chg1F6uCe251G2vFWrdQt6ZgUnP8K';
//const baseUrl = 'https://95k6wo08hg.execute-api.us-east-1.amazonaws.com/test/todos'

// AWS CDK created API Gateway
//const apiKey = 'RZluarPnz29wgpnsdJ4kya74bJbI1DaDanjWKVa6';
//const baseUrl = 'https://nrjp1jpvc8.execute-api.us-east-1.amazonaws.com/test/todos'

const todos = new Map();

let currentTodoUuid = null;
let currentItemUuid = null;

export default {
  apiKeyHeader,
  baseUrl,
  apiKey,
  todos,
  currentTodoUuid,
  currentItemUuid
}
