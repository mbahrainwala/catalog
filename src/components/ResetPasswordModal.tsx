import React, { useState, useEffect } from 'react';
import { X, Lock, Eye, EyeOff, Check, AlertCircle, Loader } from 'lucide-react';

interface ResetPasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
  token: string;
}

interface ResetPasswordForm {
  newPassword: string;
  confirmPassword: string;
}

const ResetPasswordModal: React.FC<ResetPasswordModalProps> = ({ isOpen, onClose, token }) => {
  const [loading, setLoading] = useState(false);
  const [validatingToken, setValidatingToken] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  
  const [formData, setFormData] = useState<ResetPasswordForm>({
    newPassword: '',
    confirmPassword: ''
  });

  const [validationErrors, setValidationErrors] = useState<Partial<ResetPasswordForm>>({});

  useEffect(() => {
    if (isOpen && token) {
      validateToken();
    }
  }, [isOpen, token]);

  const validateToken = async () => {
    try {
      setValidatingToken(true);
      const response = await fetch(`/api/auth/validate-reset-token?token=${encodeURIComponent(token)}`);
      const data = await response.json();
      
      setTokenValid(data.valid);
      if (!data.valid) {
        setError(data.message || 'Invalid or expired reset token');
      }
    } catch (err) {
      setTokenValid(false);
      setError('Failed to validate reset token');
    } finally {
      setValidatingToken(false);
    }
  };

  const validateForm = () => {
    const errors: Partial<ResetPasswordForm> = {};

    if (!formData.newPassword) {
      errors.newPassword = 'New password is required';
    } else if (formData.newPassword.length < 6) {
      errors.newPassword = 'New password must be at least 6 characters';
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Please confirm your new password';
    } else if (formData.newPassword !== formData.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      const response = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token,
          newPassword: formData.newPassword,
          confirmPassword: formData.confirmPassword
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setSuccess(true);
        setFormData({
          newPassword: '',
          confirmPassword: ''
        });
        setValidationErrors({});
        
        // Auto-close after 3 seconds
        setTimeout(() => {
          onClose();
          setSuccess(false);
        }, 3000);
      } else {
        setError(data.message || 'Failed to reset password');
      }
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading && !validatingToken) {
      setFormData({
        newPassword: '',
        confirmPassword: ''
      });
      setValidationErrors({});
      setError('');
      setSuccess(false);
      setTokenValid(false);
      onClose();
    }
  };

  const getPasswordStrength = (password: string) => {
    if (password.length === 0) return { strength: 0, label: '', color: '' };
    if (password.length < 6) return { strength: 1, label: 'Too short', color: 'text-red-500' };
    
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    if (score <= 2) return { strength: 2, label: 'Weak', color: 'text-orange-500' };
    if (score <= 3) return { strength: 3, label: 'Medium', color: 'text-yellow-500' };
    return { strength: 4, label: 'Strong', color: 'text-green-500' };
  };

  const passwordStrength = getPasswordStrength(formData.newPassword);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-md">
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-gray-900">Reset Password</h2>
          <button
            onClick={handleClose}
            disabled={loading || validatingToken}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50"
          >
            <X className="h-5 w-5 text-gray-500" />
          </button>
        </div>

        <div className="p-6">
          {validatingToken ? (
            <div className="text-center py-8">
              <Loader className="h-8 w-8 animate-spin text-blue-600 mx-auto mb-4" />
              <p className="text-gray-600">Validating reset token...</p>
            </div>
          ) : !tokenValid ? (
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-4 bg-red-100 rounded-full flex items-center justify-center">
                <X className="h-8 w-8 text-red-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Invalid Reset Link</h3>
              <p className="text-gray-600 mb-6">
                This password reset link is invalid or has expired. Please request a new one.
              </p>
              <button
                onClick={handleClose}
                className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Close
              </button>
            </div>
          ) : success ? (
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-4 bg-green-100 rounded-full flex items-center justify-center">
                <Check className="h-8 w-8 text-green-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">Password Reset Successful!</h3>
              <p className="text-gray-600 mb-6">
                Your password has been successfully reset. You can now log in with your new password.
              </p>
              <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                <p className="text-sm text-green-700">
                  This window will close automatically in a few seconds...
                </p>
              </div>
            </div>
          ) : (
            <>
              <div className="text-center mb-6">
                <div className="w-16 h-16 mx-auto mb-4 bg-blue-100 rounded-full flex items-center justify-center">
                  <Lock className="h-8 w-8 text-blue-600" />
                </div>
                <p className="text-gray-600">
                  Enter your new password below.
                </p>
              </div>

              {error && (
                <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg flex items-center space-x-2">
                  <AlertCircle className="h-5 w-5" />
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                {/* New Password */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    New Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <input
                      type={showNewPassword ? 'text' : 'password'}
                      required
                      minLength={6}
                      value={formData.newPassword}
                      onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                      className={`w-full pl-10 pr-10 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                        validationErrors.newPassword ? 'border-red-300' : 'border-gray-300'
                      }`}
                      placeholder="Enter your new password"
                    />
                    <button
                      type="button"
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showNewPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                    </button>
                  </div>
                  
                  {/* Password Strength Indicator */}
                  {formData.newPassword && (
                    <div className="mt-2">
                      <div className="flex items-center space-x-2">
                        <div className="flex-1 bg-gray-200 rounded-full h-2">
                          <div
                            className={`h-2 rounded-full transition-all duration-300 ${
                              passwordStrength.strength === 1 ? 'bg-red-500 w-1/4' :
                              passwordStrength.strength === 2 ? 'bg-orange-500 w-2/4' :
                              passwordStrength.strength === 3 ? 'bg-yellow-500 w-3/4' :
                              passwordStrength.strength === 4 ? 'bg-green-500 w-full' : 'w-0'
                            }`}
                          />
                        </div>
                        <span className={`text-sm font-medium ${passwordStrength.color}`}>
                          {passwordStrength.label}
                        </span>
                      </div>
                    </div>
                  )}
                  
                  {validationErrors.newPassword && (
                    <p className="text-red-500 text-sm mt-1">{validationErrors.newPassword}</p>
                  )}
                </div>

                {/* Confirm Password */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Confirm New Password
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      required
                      value={formData.confirmPassword}
                      onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                      className={`w-full pl-10 pr-10 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all ${
                        validationErrors.confirmPassword ? 'border-red-300' : 'border-gray-300'
                      }`}
                      placeholder="Confirm your new password"
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                    </button>
                  </div>
                  {validationErrors.confirmPassword && (
                    <p className="text-red-500 text-sm mt-1">{validationErrors.confirmPassword}</p>
                  )}
                </div>

                {/* Password Requirements */}
                <div className="bg-gray-50 rounded-lg p-3">
                  <h4 className="text-sm font-medium text-gray-700 mb-2">Password Requirements:</h4>
                  <ul className="text-xs text-gray-600 space-y-1">
                    <li className="flex items-center space-x-2">
                      <div className={`w-2 h-2 rounded-full ${formData.newPassword.length >= 6 ? 'bg-green-500' : 'bg-gray-300'}`} />
                      <span>At least 6 characters</span>
                    </li>
                    <li className="flex items-center space-x-2">
                      <div className={`w-2 h-2 rounded-full ${/[A-Z]/.test(formData.newPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
                      <span>One uppercase letter (recommended)</span>
                    </li>
                    <li className="flex items-center space-x-2">
                      <div className={`w-2 h-2 rounded-full ${/[0-9]/.test(formData.newPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
                      <span>One number (recommended)</span>
                    </li>
                    <li className="flex items-center space-x-2">
                      <div className={`w-2 h-2 rounded-full ${/[^A-Za-z0-9]/.test(formData.newPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
                      <span>One special character (recommended)</span>
                    </li>
                  </ul>
                </div>

                <div className="flex space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={handleClose}
                    disabled={loading}
                    className="flex-1 px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Resetting...' : 'Reset Password'}
                  </button>
                </div>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordModal;