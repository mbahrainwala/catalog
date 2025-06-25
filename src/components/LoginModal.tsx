import React, { useState } from 'react';
import { X, User, Lock, Mail, UserPlus } from 'lucide-react';
import ForgotPasswordModal from './ForgotPasswordModal';
import AccountActivationModal from './AccountActivationModal';

interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
  onLogin: (token: string, user: any) => void;
}

interface LoginForm {
  username: string;
  password: string;
}

interface SignupForm {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}

const LoginModal: React.FC<LoginModalProps> = ({ isOpen, onClose, onLogin }) => {
  const [isSignup, setIsSignup] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [showAccountActivation, setShowAccountActivation] = useState(false);
  const [activationUsername, setActivationUsername] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [loginForm, setLoginForm] = useState<LoginForm>({
    username: '',
    password: ''
  });
  
  const [signupForm, setSignupForm] = useState<SignupForm>({
    username: '',
    email: '',
    firstName: '',
    lastName: ''
  });

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('/api/auth/signin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginForm),
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem('token', data.token);
        onLogin(data.token, data);
        onClose();
        setLoginForm({ username: '', password: '' });
      } else if (response.status === 202 && data.needsActivation) {
        // Account needs activation
        setActivationUsername(data.username);
        setShowAccountActivation(true);
        setError('');
      } else {
        setError(data.message || 'Login failed');
      }
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSignupSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(signupForm),
      });

      const data = await response.json();

      if (response.ok) {
        setIsSignup(false);
        setSignupForm({
          username: '',
          email: '',
          firstName: '',
          lastName: ''
        });
        setError('');
        alert('Account created successfully! Please check your email for activation instructions. You have 48 hours to activate your account.');
      } else {
        setError(data.message || 'Signup failed');
      }
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = () => {
    setShowForgotPassword(true);
  };

  const handleBackToLogin = () => {
    setShowForgotPassword(false);
    setShowAccountActivation(false);
  };

  const handleAccountActivated = () => {
    setShowAccountActivation(false);
    setActivationUsername('');
    alert('Account activated successfully! You can now log in with your new password.');
  };

  if (!isOpen) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-md">
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <h2 className="text-2xl font-bold text-gray-900">
              {isSignup ? 'Create Account' : 'Welcome Back'}
            </h2>
            <button
              onClick={onClose}
              className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <X className="h-5 w-5 text-gray-500" />
            </button>
          </div>

          <div className="p-6">
            {error && (
              <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg">
                {error}
              </div>
            )}

            {isSignup ? (
              <form onSubmit={handleSignupSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      First Name
                    </label>
                    <input
                      type="text"
                      required
                      value={signupForm.firstName}
                      onChange={(e) => setSignupForm({ ...signupForm, firstName: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Last Name
                    </label>
                    <input
                      type="text"
                      required
                      value={signupForm.lastName}
                      onChange={(e) => setSignupForm({ ...signupForm, lastName: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Username
                  </label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <input
                      type="text"
                      required
                      value={signupForm.username}
                      onChange={(e) => setSignupForm({ ...signupForm, username: e.target.value })}
                      className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Email
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <input
                      type="email"
                      required
                      value={signupForm.email}
                      onChange={(e) => setSignupForm({ ...signupForm, email: e.target.value })}
                      className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-start space-x-3">
                    <Mail className="h-5 w-5 text-blue-600 mt-0.5" />
                    <div>
                      <p className="text-sm font-medium text-blue-900">Account Activation Required</p>
                      <p className="text-sm text-blue-700 mt-1">
                        A temporary password will be sent to your email. You must activate your account within 48 hours.
                      </p>
                    </div>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Creating Account...' : 'Create Account'}
                </button>
              </form>
            ) : (
              <form onSubmit={handleLoginSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Username
                  </label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <input
                      type="text"
                      required
                      value={loginForm.username}
                      onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })}
                      className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Enter your username"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <input
                      type="password"
                      required
                      value={loginForm.password}
                      onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                      className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Enter your password"
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div className="text-sm">
                    <button
                      type="button"
                      onClick={handleForgotPassword}
                      className="text-blue-600 hover:text-blue-700 font-medium"
                    >
                      Forgot your password?
                    </button>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Signing In...' : 'Sign In'}
                </button>
              </form>
            )}

            <div className="mt-6 text-center">
              <button
                onClick={() => {
                  setIsSignup(!isSignup);
                  setError('');
                }}
                className="text-blue-600 hover:text-blue-700 font-medium"
              >
                {isSignup ? 'Already have an account? Sign In' : "Don't have an account? Sign Up"}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Forgot Password Modal */}
      <ForgotPasswordModal
        isOpen={showForgotPassword}
        onClose={() => setShowForgotPassword(false)}
        onBackToLogin={handleBackToLogin}
      />

      {/* Account Activation Modal */}
      <AccountActivationModal
        isOpen={showAccountActivation}
        onClose={() => setShowAccountActivation(false)}
        onBackToLogin={handleBackToLogin}
        onAccountActivated={handleAccountActivated}
        username={activationUsername}
      />
    </>
  );
};

export default LoginModal;